package com.unal.nachoquest.domain.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Query
import com.unal.nachoquest.domain.model.JugadorRanking
import com.unal.nachoquest.domain.model.TriviaDiaria
import com.unal.nachoquest.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "UserRepository"

// Nombre que se guardaba cuando el registro no alcanzaba a enviar el nombre del jugador
private const val NOMBRE_PROVISIONAL = "Estudiante UNAL"

/**
 * Progreso del jugador en Firestore (documento users/{uid}).
 *
 * Mientras hay sesión, el perfil se escucha en tiempo real: llega primero de la caché local
 * (funciona sin conexión) y después del servidor. Las escrituras solo envían los campos que
 * cambian (arrayUnion, increment, merge), así que nunca pisan progreso que aún no ha cargado;
 * Firestore las aplica al instante en la caché y las sube cuando vuelve la conexión.
 */
@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")
    // Datos públicos de cada jugador (nombre, XP, nivel y estrellas) para la tabla de posiciones
    private val rankingCollection = firestore.collection("ranking")

    private val _userProfileFlow = MutableStateFlow(UserProfile())
    val userProfileFlow: StateFlow<UserProfile> = _userProfileFlow.asStateFlow()

    // Indica si ya llegó el perfil del jugador (para no mostrar el prólogo a quien ya lo vio)
    private val _perfilCargado = MutableStateFlow(false)
    val perfilCargado: StateFlow<Boolean> = _perfilCargado.asStateFlow()

    // Solo se tocan desde el hilo principal (ahí llegan los callbacks de Firebase)
    private var uidActual: String? = null
    private var escuchaPerfil: ListenerRegistration? = null
    // Usuario cuyo documento ya se creó (un evento atrasado no debe volver a crearlo con otro nombre)
    private var perfilCreadoPara: String? = null
    // Usuario cuya fila del ranking ya se puso al día en esta sesión
    private var rankingPublicadoPara: String? = null

    init {
        // Se dispara al abrir la app con sesión guardada, al iniciar sesión y al cerrarla
        auth.addAuthStateListener { observarPerfil(it.currentUser) }
    }

    private fun observarPerfil(usuario: FirebaseUser?) {
        if (usuario?.uid == uidActual) return
        escuchaPerfil?.remove()
        escuchaPerfil = null
        uidActual = usuario?.uid
        // Se olvida el perfil anterior para no mezclarlo con el del siguiente usuario
        _userProfileFlow.value = UserProfile()
        _perfilCargado.value = false
        if (usuario == null) return

        escuchaPerfil = usersCollection.document(usuario.uid).addSnapshotListener { snapshot, error ->
            if (usuario.uid != uidActual) return@addSnapshotListener
            if (error != null || snapshot == null) {
                Log.w(TAG, "No se pudo leer el perfil", error)
                return@addSnapshotListener
            }
            if (snapshot.exists()) {
                val perfil = try {
                    snapshot.toObject(UserProfile::class.java)
                } catch (e: RuntimeException) {
                    Log.e(TAG, "El perfil guardado tiene un formato inesperado", e)
                    null
                } ?: return@addSnapshotListener
                _userProfileFlow.value = perfil.normalizado(usuario.uid)
                _perfilCargado.value = true
                repararNombre(usuario, perfil)
                if (rankingPublicadoPara != usuario.uid) {
                    rankingPublicadoPara = usuario.uid
                    publicarEnRanking()
                }
            } else if (!snapshot.metadata.isFromCache && perfilCreadoPara != usuario.uid) {
                // Jugador nuevo (confirmado por el servidor): solo se guardan sus datos de identidad
                crearPerfil(usuario, nombre = null)
            }
        }
    }

    /**
     * Crea el documento de un jugador nuevo con su nombre. Al registrarse con correo se llama con el
     * nombre elegido, sin esperar a que Firebase actualice el displayName de la cuenta.
     */
    fun crearPerfil(usuario: FirebaseUser, nombre: String?) {
        perfilCreadoPara = usuario.uid
        guardar(usuario.uid, datosIdentidad(usuario, nombre))
    }

    /**
     * Registra un reto aprobado: suma la XP, entrega ítems e insignias y guarda la mejor
     * puntuación (1-3 estrellas) obtenida en ese reto.
     */
    fun registrarRetoAprobado(
        retoId: String,
        estrellas: Int,
        xpGanada: Int,
        itemsGanados: List<String> = emptyList(),
        insigniasGanadas: List<String> = emptyList()
    ) {
        val uid = uidActual ?: return
        val actual = _userProfileFlow.value
        val mejoresEstrellas = maxOf(estrellas, actual.estrellasPorReto[retoId] ?: 0)
        val estrellasPorReto = actual.estrellasPorReto + (retoId to mejoresEstrellas)
        val nuevaXp = actual.xp + xpGanada

        _userProfileFlow.update {
            it.copy(
                xp = nuevaXp,
                nivel = nivelParaXp(nuevaXp),
                estrellasPorReto = estrellasPorReto,
                estrellas = estrellasPorReto.values.sum(),
                retosCompletados = (it.retosCompletados + retoId).distinct(),
                itemsDesbloqueados = (it.itemsDesbloqueados + itemsGanados).distinct(),
                insignias = (it.insignias + insigniasGanadas).distinct()
            )
        }

        val cambios = mutableMapOf<String, Any>(
            "retosCompletados" to FieldValue.arrayUnion(retoId),
            "estrellasPorReto" to mapOf(retoId to mejoresEstrellas),
            "estrellas" to estrellasPorReto.values.sum()
        )
        if (xpGanada > 0) {
            cambios["xp"] = FieldValue.increment(xpGanada.toLong())
            cambios["nivel"] = nivelParaXp(nuevaXp)
        }
        if (itemsGanados.isNotEmpty()) cambios["itemsDesbloqueados"] = FieldValue.arrayUnion(*itemsGanados.toTypedArray())
        if (insigniasGanadas.isNotEmpty()) cambios["insignias"] = FieldValue.arrayUnion(*insigniasGanadas.toTypedArray())
        guardar(uid, cambios)
        publicarEnRanking()
    }

    /**
     * Registra la trivia del día superada: suma la XP y actualiza la racha de días seguidos.
     * Si ya se había jugado hoy no cambia nada (es solo práctica).
     * @return la racha de días con la que queda el jugador
     */
    fun registrarTriviaDiaria(hoy: Long, xpGanada: Int, perfecta: Boolean): Int {
        val uid = uidActual ?: return 0
        val actual = _userProfileFlow.value
        if (TriviaDiaria.yaJugoHoy(actual, hoy)) return actual.rachaDias

        val racha = TriviaDiaria.rachaTrasJugar(actual, hoy)
        val mejorRacha = maxOf(racha, actual.mejorRachaDias)
        val nuevaXp = actual.xp + xpGanada
        _userProfileFlow.update {
            it.copy(
                xp = nuevaXp,
                nivel = nivelParaXp(nuevaXp),
                rachaDias = racha,
                mejorRachaDias = mejorRacha,
                ultimoDiaTrivia = hoy,
                triviasSuperadas = it.triviasSuperadas + 1,
                triviasPerfectas = it.triviasPerfectas + if (perfecta) 1 else 0
            )
        }

        val cambios = mutableMapOf<String, Any>(
            "rachaDias" to racha,
            "mejorRachaDias" to mejorRacha,
            "ultimoDiaTrivia" to hoy,
            "triviasSuperadas" to FieldValue.increment(1),
            "xp" to FieldValue.increment(xpGanada.toLong()),
            "nivel" to nivelParaXp(nuevaXp)
        )
        if (perfecta) cambios["triviasPerfectas"] = FieldValue.increment(1)
        guardar(uid, cambios)
        publicarEnRanking()
        return racha
    }

    fun marcarEscenaVista(escenaId: String) {
        val uid = uidActual ?: return
        if (escenaId in _userProfileFlow.value.escenasVistas) return
        _userProfileFlow.update { it.copy(escenasVistas = it.escenasVistas + escenaId) }
        guardar(uid, mapOf("escenasVistas" to FieldValue.arrayUnion(escenaId)))
    }

    /**
     * Anota un lugar del mapa como descubierto y da XP la primera vez.
     * @return true si el lugar era nuevo para el jugador
     */
    fun registrarLugarDescubierto(puntoId: String): Boolean {
        val uid = uidActual ?: return false
        // Sin el perfil cargado no se sabe si el lugar es nuevo, y la XP se sumaría dos veces
        if (!_perfilCargado.value) return false
        val actual = _userProfileFlow.value
        if (puntoId in actual.lugaresDescubiertos) return false

        val nuevaXp = actual.xp + XP_POR_LUGAR
        _userProfileFlow.update {
            it.copy(
                lugaresDescubiertos = it.lugaresDescubiertos + puntoId,
                xp = nuevaXp,
                nivel = nivelParaXp(nuevaXp)
            )
        }
        guardar(
            uid,
            mapOf(
                "lugaresDescubiertos" to FieldValue.arrayUnion(puntoId),
                "xp" to FieldValue.increment(XP_POR_LUGAR.toLong()),
                "nivel" to nivelParaXp(nuevaXp)
            )
        )
        publicarEnRanking()
        return true
    }

    /**
     * Borra el progreso del jugador (XP, historia, ítems y rachas) y conserva su nombre y su
     * correo. Sirve para volver a vivir la historia desde el prólogo.
     */
    fun reiniciarProgreso() {
        val uid = uidActual ?: return
        val actual = _userProfileFlow.value
        val nuevo = UserProfile(id = uid, nombre = actual.nombre, email = actual.email)
        _userProfileFlow.value = nuevo
        // Sin merge: el documento completo se reemplaza por el perfil vacío
        usersCollection.document(uid).set(nuevo)
            .addOnFailureListener { Log.w(TAG, "No se pudo reiniciar el progreso", it) }
        publicarEnRanking()
    }

    /** Los mejores jugadores por XP. Lanza una excepción si no hay conexión o permisos. */
    suspend fun obtenerRanking(limite: Long = 30): List<JugadorRanking> {
        val uid = uidActual
        return rankingCollection.orderBy("xp", Query.Direction.DESCENDING).limit(limite).get().await()
            .documents.map { doc ->
                JugadorRanking(
                    nombre = doc.getString("nombre").orEmpty().ifBlank { "Explorador" },
                    xp = (doc.getLong("xp") ?: 0L).toInt(),
                    nivel = (doc.getLong("nivel") ?: 1L).toInt(),
                    estrellas = (doc.getLong("estrellas") ?: 0L).toInt(),
                    esJugadorActual = doc.id == uid
                )
            }
    }

    // La fila del ranking solo lleva datos que el jugador acepta mostrar: nunca el correo
    private fun publicarEnRanking() {
        val uid = uidActual ?: return
        val perfil = _userProfileFlow.value
        rankingCollection.document(uid)
            .set(
                mapOf(
                    "nombre" to perfil.nombre.ifBlank { "Explorador" },
                    "xp" to perfil.xp,
                    "nivel" to nivelParaXp(perfil.xp),
                    "estrellas" to perfil.estrellasPorReto.values.sum()
                )
            )
            .addOnFailureListener { Log.w(TAG, "No se pudo actualizar el ranking", it) }
    }

    /** Corrige perfiles que quedaron con el nombre provisional por el registro anterior. */
    private fun repararNombre(usuario: FirebaseUser, perfil: UserProfile) {
        val nombreCuenta = usuario.displayName?.trim().orEmpty()
        val provisional = perfil.nombre.isBlank() || perfil.nombre == NOMBRE_PROVISIONAL
        if (provisional && nombreCuenta.isNotBlank() && nombreCuenta != perfil.nombre) {
            guardar(usuario.uid, mapOf("nombre" to nombreCuenta))
        }
    }

    private fun datosIdentidad(usuario: FirebaseUser, nombre: String?): Map<String, Any> = mapOf(
        "id" to usuario.uid,
        "nombre" to (
            nombre?.trim()?.takeIf { it.isNotBlank() }
                ?: usuario.displayName?.trim()?.takeIf { it.isNotBlank() }
                ?: usuario.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
                ?: "Explorador"
            ),
        "email" to usuario.email.orEmpty()
    )

    // Sin await: el cambio queda en la caché local al instante y se sube cuando hay conexión
    private fun guardar(uid: String, cambios: Map<String, Any>) {
        usersCollection.document(uid).set(cambios, SetOptions.merge())
            .addOnFailureListener { Log.w(TAG, "No se pudo guardar el progreso", it) }
    }

    companion object {
        const val XP_POR_NIVEL = 100
        const val XP_POR_LUGAR = 5

        fun nivelParaXp(xp: Int): Int = xp / XP_POR_NIVEL + 1
    }
}

// El nivel y el total de estrellas se derivan de la XP y de las estrellas por reto
private fun UserProfile.normalizado(uid: String): UserProfile = copy(
    id = uid,
    nivel = UserRepository.nivelParaXp(xp),
    estrellas = estrellasPorReto.values.sum()
)
