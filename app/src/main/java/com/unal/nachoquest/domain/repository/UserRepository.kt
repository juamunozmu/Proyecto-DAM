package com.unal.nachoquest.domain.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unal.nachoquest.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    // Evita que dos escrituras simultáneas (p. ej. reto + escena vista) se pisen entre sí
    private val mutex = Mutex()

    // Perfil en memoria/reactivo para que la app siempre tenga datos consistentes (incluso offline)
    private val _userProfileFlow = MutableStateFlow(perfilLocal())
    val userProfileFlow: StateFlow<UserProfile> = _userProfileFlow.asStateFlow()

    // Indica si ya se intentó cargar el perfil remoto (para no mostrar el prólogo a quien ya lo vio)
    private val _perfilCargado = MutableStateFlow(false)
    val perfilCargado: StateFlow<Boolean> = _perfilCargado.asStateFlow()

    private fun perfilLocal() = UserProfile(
        id = "local_student",
        nombre = "Estudiante UNAL",
        email = "usuario@unal.edu.co"
    )

    suspend fun getUserProfile(): UserProfile? {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _perfilCargado.value = true
            return _userProfileFlow.value
        }

        return try {
            val snapshot = usersCollection.document(userId).get().await()
            val profile = if (snapshot.exists()) {
                snapshot.toObject(UserProfile::class.java)?.copy(id = userId)
            } else {
                val newProfile = UserProfile(
                    id = userId,
                    nombre = auth.currentUser?.displayName ?: "Estudiante UNAL",
                    email = auth.currentUser?.email ?: ""
                )
                usersCollection.document(userId).set(newProfile).await()
                newProfile
            }

            if (profile != null) {
                _userProfileFlow.value = profile
            }
            profile ?: _userProfileFlow.value
        } catch (e: Exception) {
            _userProfileFlow.value
        } finally {
            _perfilCargado.value = true
        }
    }

    /**
     * Registra el resultado de un reto. Solo un reto aprobado cuenta como completado y entrega
     * ítems; las estrellas guardan siempre la mejor puntuación obtenida.
     */
    suspend fun registrarResultadoReto(
        retoId: String,
        aprobado: Boolean,
        estrellas: Int,
        xpGanada: Int,
        itemsGanados: List<String> = emptyList(),
        insigniasGanadas: List<String> = emptyList()
    ) {
        if (!aprobado && xpGanada == 0) return

        actualizarPerfil { actual ->
            val nuevaXp = actual.xp + xpGanada
            val estrellasPorReto = if (aprobado) {
                actual.estrellasPorReto + (retoId to maxOf(estrellas, actual.estrellasPorReto[retoId] ?: 0))
            } else {
                actual.estrellasPorReto
            }

            actual.copy(
                xp = nuevaXp,
                nivel = nivelParaXp(nuevaXp),
                estrellasPorReto = estrellasPorReto,
                estrellas = estrellasPorReto.values.sum(),
                retosCompletados = if (aprobado) (actual.retosCompletados + retoId).distinct() else actual.retosCompletados,
                itemsDesbloqueados = (actual.itemsDesbloqueados + itemsGanados).distinct(),
                insignias = (actual.insignias + insigniasGanadas).distinct()
            )
        }
    }

    suspend fun marcarEscenaVista(escenaId: String) {
        if (escenaId in _userProfileFlow.value.escenasVistas) return
        actualizarPerfil { it.copy(escenasVistas = (it.escenasVistas + escenaId).distinct()) }
    }

    suspend fun registrarLugarDescubierto(puntoId: String) {
        if (puntoId in _userProfileFlow.value.lugaresDescubiertos) return
        actualizarPerfil { it.copy(lugaresDescubiertos = (it.lugaresDescubiertos + puntoId).distinct()) }
    }

    /** Olvida el perfil en memoria al cerrar sesión, para no mezclarlo con el del siguiente usuario. */
    fun limpiarSesion() {
        _userProfileFlow.value = perfilLocal()
        _perfilCargado.value = false
    }

    private suspend fun actualizarPerfil(transformar: (UserProfile) -> UserProfile) {
        // Sin el perfil remoto cargado, se sobrescribiría Firestore con un perfil vacío
        if (!_perfilCargado.value) getUserProfile()
        mutex.withLock {
            val actualizado = transformar(_userProfileFlow.value)
            _userProfileFlow.value = actualizado

            val userId = auth.currentUser?.uid ?: return
            try {
                usersCollection.document(userId).set(actualizado).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val XP_POR_NIVEL = 100

        fun nivelParaXp(xp: Int): Int = xp / XP_POR_NIVEL + 1
    }
}
