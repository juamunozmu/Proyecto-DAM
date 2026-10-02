package com.unal.nachoquest.ui.screens.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class AuthUiState(
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val nombreUsuarioActual: String = ""
)

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow(AuthUiState(
        isAuthenticated = auth.currentUser != null,
        nombreUsuarioActual = auth.currentUser?.displayName ?: auth.currentUser?.email?.substringBefore("@") ?: ""
    ))
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    fun onNombreChange(nombre: String) {
        _authState.update { it.copy(nombre = nombre, error = null) }
    }

    fun onEmailChange(email: String) {
        _authState.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChange(password: String) {
        _authState.update { it.copy(password = password, error = null) }
    }

    fun login() {
        val state = _authState.value
        if (!state.email.contains("@") || !state.email.contains(".")) {
            _authState.update { it.copy(error = "Correo electrónico inválido") }
            return
        }
        if (state.password.length < 6) {
            _authState.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
            return
        }

        _authState.update { it.copy(isLoading = true, error = null) }

        auth.signInWithEmailAndPassword(state.email, state.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            nombreUsuarioActual = user?.displayName ?: user?.email?.substringBefore("@") ?: ""
                        )
                    }
                } else {
                    _authState.update {
                        it.copy(isLoading = false, error = task.exception?.message ?: "Error al iniciar sesión")
                    }
                }
            }
    }

    fun register() {
        val state = _authState.value
        if (state.nombre.isBlank()) {
            _authState.update { it.copy(error = "El nombre de usuario es obligatorio") }
            return
        }
        if (!state.email.contains("@") || !state.email.contains(".")) {
            _authState.update { it.copy(error = "Correo electrónico inválido") }
            return
        }
        if (state.password.length < 6) {
            _authState.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
            return
        }

        _authState.update { it.copy(isLoading = true, error = null) }

        auth.createUserWithEmailAndPassword(state.email, state.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // Actualizar el perfil con el nombre (opcional)
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(state.nombre)
                        .build()
                    user?.updateProfile(profileUpdates)

                    _authState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            nombreUsuarioActual = state.nombre
                        )
                    }
                } else {
                    _authState.update {
                        it.copy(isLoading = false, error = task.exception?.message ?: "Error al registrar")
                    }
                }
            }
    }

    fun firebaseAuthWithGoogle(idToken: String) {
        _authState.update { it.copy(isLoading = true, error = null) }
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            nombreUsuarioActual = user?.displayName ?: user?.email?.substringBefore("@") ?: ""
                        )
                    }
                } else {
                    _authState.update {
                        it.copy(isLoading = false, error = task.exception?.message ?: "Error con Google Login")
                    }
                }
            }
    }

    fun cerrarSesion() {
        auth.signOut()
        _authState.update { AuthUiState() }
    }
}