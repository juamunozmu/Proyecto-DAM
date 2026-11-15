package com.unal.nachoquest.ui.screens.auth

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.unal.nachoquest.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TAG = "AuthViewModel"
private const val LONGITUD_MINIMA_CONTRASENA = 6
private val PATRON_CORREO = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

data class AuthUiState(
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    // Avisos que no son errores (p. ej. el correo de recuperación enviado)
    val mensaje: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthUiState(isAuthenticated = auth.currentUser != null))
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

    /** Borra errores y avisos al pasar entre el inicio de sesión y el registro. */
    fun limpiarMensajes() {
        _authState.update { it.copy(error = null, mensaje = null) }
    }

    fun mostrarError(mensaje: String) {
        _authState.update { it.copy(error = mensaje, mensaje = null) }
    }

    fun login() {
        val state = _authState.value
        val email = state.email.trim()
        val error = validarCorreo(email) ?: validarContrasena(state.password)
        if (error != null) return mostrarError(error)

        autenticar { auth.signInWithEmailAndPassword(email, state.password).await() }
    }

    fun register() {
        val state = _authState.value
        val nombre = state.nombre.trim()
        val email = state.email.trim()
        val error = validarNombre(nombre) ?: validarCorreo(email) ?: validarContrasena(state.password)
        if (error != null) return mostrarError(error)

        autenticar {
            val usuario = auth.createUserWithEmailAndPassword(email, state.password).await().user
                ?: return@autenticar
            // El perfil se crea ya con el nombre elegido (antes podía quedar como "Estudiante UNAL")
            userRepository.crearPerfil(usuario, nombre)
            usuario.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(nombre).build())
                .addOnFailureListener { Log.w(TAG, "No se pudo guardar el nombre en la cuenta", it) }
        }
    }

    fun iniciarSesionConGoogle(idToken: String) {
        autenticar { auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await() }
    }

    /** Usa el flujo web de Firebase: requiere habilitar el proveedor de Apple en la consola de Firebase. */
    fun iniciarSesionConApple(activity: Activity) {
        autenticar {
            // Si Android cerró la app mientras estaba abierto el flujo web, el resultado queda pendiente
            val pendiente = auth.pendingAuthResult
            if (pendiente != null) {
                pendiente.await()
            } else {
                val proveedor = OAuthProvider.newBuilder("apple.com")
                    .setScopes(listOf("email", "name"))
                    .build()
                auth.startActivityForSignInWithProvider(activity, proveedor).await()
            }
        }
    }

    fun recuperarContrasena() {
        val state = _authState.value
        val email = state.email.trim()
        if (validarCorreo(email) != null) {
            return mostrarError("Escribe tu correo arriba para enviarte el enlace de recuperación")
        }
        if (state.isLoading) return

        _authState.update { it.copy(isLoading = true, error = null, mensaje = null) }
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                _authState.update {
                    it.copy(
                        isLoading = false,
                        mensaje = "Si hay una cuenta con $email, te enviamos un enlace para cambiar la contraseña."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _authState.update { it.copy(isLoading = false, error = mensajeDeError(e)) }
            }
        }
    }

    fun cerrarSesion() {
        // El repositorio escucha el cambio de sesión y olvida el perfil del jugador
        auth.signOut()
        _authState.value = AuthUiState()
    }

    private fun autenticar(accion: suspend () -> Unit) {
        if (_authState.value.isLoading) return
        _authState.update { it.copy(isLoading = true, error = null, mensaje = null) }
        viewModelScope.launch {
            try {
                accion()
                _authState.update {
                    it.copy(isLoading = false, isAuthenticated = auth.currentUser != null, password = "")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Error de autenticación", e)
                _authState.update { it.copy(isLoading = false, error = mensajeDeError(e)) }
            }
        }
    }
}

internal fun validarNombre(nombre: String): String? =
    if (nombre.isBlank()) "El nombre de usuario es obligatorio" else null

internal fun validarCorreo(email: String): String? =
    if (!PATRON_CORREO.matches(email)) "Correo electrónico inválido" else null

internal fun validarContrasena(password: String): String? =
    if (password.length < LONGITUD_MINIMA_CONTRASENA) {
        "La contraseña debe tener al menos $LONGITUD_MINIMA_CONTRASENA caracteres"
    } else {
        null
    }

/** Firebase responde en inglés; se traducen los casos que el jugador puede resolver. */
private fun mensajeDeError(e: Exception): String = when (e) {
    is FirebaseNetworkException -> "Sin conexión a internet. Revisa tu red e inténtalo de nuevo."
    is FirebaseTooManyRequestsException -> "Demasiados intentos. Espera un momento y vuelve a intentarlo."
    is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil: usa al menos $LONGITUD_MINIMA_CONTRASENA caracteres."
    is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo. Inicia sesión o usa otro método."
    is FirebaseAuthInvalidUserException -> "No encontramos una cuenta activa con ese correo."
    is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos."
    is FirebaseAuthException -> when (e.errorCode) {
        "ERROR_OPERATION_NOT_ALLOWED" -> "Este método de acceso aún no está habilitado. Usa tu correo o Google."
        "ERROR_WEB_CONTEXT_CANCELED" -> "Cancelaste el inicio de sesión."
        // Pasa con compilaciones cuya llave de firma no está registrada en el proyecto de Firebase
        "ERROR_INVALID_CERT_HASH" -> "Esta copia de la app no está registrada en Firebase (falta su huella SHA-1)."
        else -> "No pudimos iniciar sesión (${e.errorCode})."
    }
    else -> "Algo salió mal. Inténtalo de nuevo."
}
