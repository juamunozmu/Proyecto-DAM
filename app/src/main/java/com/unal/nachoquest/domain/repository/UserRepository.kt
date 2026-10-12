package com.unal.nachoquest.domain.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unal.nachoquest.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    // Perfil en memoria/reactivo para que la app siempre tenga datos consistentes (incluso offline)
    private val _userProfileFlow = MutableStateFlow(
        UserProfile(
            id = "local_student",
            nombre = "Estudiante UNAL",
            email = "usuario@unal.edu.co",
            xp = 0,
            nivel = 1,
            estrellas = 0,
            retosCompletados = emptyList(),
            itemsDesbloqueados = emptyList(),
            insignias = emptyList()
        )
    )
    val userProfileFlow: StateFlow<UserProfile> = _userProfileFlow.asStateFlow()

    suspend fun getUserProfile(): UserProfile? {
        val userId = auth.currentUser?.uid
        if (userId == null) {
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
        }
    }

    suspend fun updateXP(xpGanada: Int, retoId: String) {
        registrarRetoCompletado(retoId = retoId, xpGanada = xpGanada)
    }

    suspend fun registrarRetoCompletado(
        retoId: String,
        xpGanada: Int,
        itemsGanados: List<String> = emptyList(),
        insigniasGanadas: List<String> = emptyList()
    ) {
        val currentProfile = getUserProfile() ?: _userProfileFlow.value

        val nuevaXp = currentProfile.xp + xpGanada
        val nuevoNivel = (nuevaXp / 100) + 1
        val nuevasEstrellas = currentProfile.estrellas + 1

        val nuevosRetos = currentProfile.retosCompletados.toMutableSet().apply {
            add(retoId)
        }.toList()

        val nuevosItems = currentProfile.itemsDesbloqueados.toMutableSet().apply {
            addAll(itemsGanados)
        }.toList()

        val nuevasInsignias = currentProfile.insignias.toMutableSet().apply {
            addAll(insigniasGanadas)
        }.toList()

        val updatedProfile = currentProfile.copy(
            xp = nuevaXp,
            nivel = nuevoNivel,
            estrellas = nuevasEstrellas,
            retosCompletados = nuevosRetos,
            itemsDesbloqueados = nuevosItems,
            insignias = nuevasInsignias
        )

        _userProfileFlow.value = updatedProfile

        val userId = auth.currentUser?.uid
        if (userId != null) {
            try {
                usersCollection.document(userId).set(updatedProfile).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}