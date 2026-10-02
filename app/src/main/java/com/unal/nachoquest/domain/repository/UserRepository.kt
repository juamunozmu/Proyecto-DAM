package com.unal.nachoquest.domain.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unal.nachoquest.domain.model.UserProfile
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun getUserProfile(): UserProfile? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            if (snapshot.exists()) {
                snapshot.toObject(UserProfile::class.java)?.copy(id = userId)
            } else {
                // Crear perfil inicial si no existe
                val newProfile = UserProfile(
                    id = userId,
                    nombre = auth.currentUser?.displayName ?: "Estudiante",
                    email = auth.currentUser?.email ?: ""
                )
                usersCollection.document(userId).set(newProfile).await()
                newProfile
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateXP(xpGanada: Int, retoId: String) {
        val userId = auth.currentUser?.uid ?: return
        try {
            val currentProfile = getUserProfile() ?: return
            
            // Lógica simple de niveles (1 nivel cada 100 XP)
            val nuevaXp = currentProfile.xp + xpGanada
            val nuevoNivel = (nuevaXp / 100) + 1
            
            // Añadir reto a completados si no estaba
            val nuevosRetos = currentProfile.retosCompletados.toMutableList()
            if (!nuevosRetos.contains(retoId)) {
                nuevosRetos.add(retoId)
            }

            usersCollection.document(userId).update(
                mapOf(
                    "xp" to nuevaXp,
                    "nivel" to nuevoNivel,
                    "retosCompletados" to nuevosRetos
                )
            ).await()
        } catch (e: Exception) {
            // Error al actualizar
        }
    }
}