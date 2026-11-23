package com.unal.nachoquest.ui.screens.auth

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.unal.nachoquest.domain.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Validación del inicio de sesión y del registro, con la pantalla y el ViewModel reales.
 * Firebase está simulado: los datos inválidos nunca deben llegar a él.
 */
@RunWith(AndroidJUnit4::class)
class AccesoUiTest {

    @get:Rule
    val compose = createComposeRule()

    private val auth = mockk<FirebaseAuth>(relaxed = true) {
        every { currentUser } returns null
    }
    private val viewModel = AuthViewModel(auth, mockk<UserRepository>(relaxed = true))

    private fun abrirInicioDeSesion() = compose.setContent {
        LoginScreen(viewModel = viewModel, onLoginExitoso = {}, onIrARegistro = {})
    }

    private fun abrirRegistro() = compose.setContent {
        RegisterScreen(viewModel = viewModel, onRegistroExitoso = {}, onIrALogin = {})
    }

    private fun escribir(campo: String, texto: String) {
        compose.onNodeWithText(campo).performScrollTo().performTextInput(texto)
    }

    private fun tocar(texto: String) {
        compose.onNodeWithText(texto).performScrollTo().performClick()
    }

    // ---------- Inicio de sesión ----------

    @Test
    fun correoInvalido_muestraErrorYNoLlamaAFirebase() {
        abrirInicioDeSesion()
        escribir(CAMPO_CORREO, "ana.unal.edu.co")
        escribir(CAMPO_CONTRASENA, "secreta1")
        tocar("Iniciar sesión")

        compose.onNodeWithText("Correo electrónico inválido").assertExists()
        verify(exactly = 0) { auth.signInWithEmailAndPassword(any(), any()) }
    }

    @Test
    fun contrasenaCorta_muestraError() {
        abrirInicioDeSesion()
        escribir(CAMPO_CORREO, "ana@unal.edu.co")
        escribir(CAMPO_CONTRASENA, "123")
        tocar("Iniciar sesión")

        compose.onNodeWithText("La contraseña debe tener al menos 6 caracteres").assertExists()
        verify(exactly = 0) { auth.signInWithEmailAndPassword(any(), any()) }
    }

    @Test
    fun alCorregirElCampo_desapareceElError() {
        abrirInicioDeSesion()
        tocar("Iniciar sesión")
        compose.onNodeWithText("Correo electrónico inválido").assertExists()

        escribir(CAMPO_CORREO, "a")

        compose.onNodeWithText("Correo electrónico inválido").assertDoesNotExist()
    }

    @Test
    fun datosValidos_inicianSesionConElCorreoSinEspacios() {
        abrirInicioDeSesion()
        escribir(CAMPO_CORREO, "  ana@unal.edu.co ")
        escribir(CAMPO_CONTRASENA, "secreta1")
        tocar("Iniciar sesión")

        verify(exactly = 1) { auth.signInWithEmailAndPassword("ana@unal.edu.co", "secreta1") }
        // Mientras Firebase responde, la pantalla queda cargando y no deja pedir otra vez
        assertTrue(viewModel.authState.value.isLoading)
        compose.onNodeWithText("¿Olvidaste tu contraseña?").assertIsNotEnabled()
    }

    @Test
    fun recuperarContrasenaSinCorreo_pideEscribirloPrimero() {
        abrirInicioDeSesion()
        tocar("¿Olvidaste tu contraseña?")

        compose.onNodeWithText("Escribe tu correo arriba para enviarte el enlace de recuperación").assertExists()
        verify(exactly = 0) { auth.sendPasswordResetEmail(any()) }
    }

    // ---------- Registro ----------

    @Test
    fun registroSinNombre_muestraError() {
        abrirRegistro()
        escribir(CAMPO_CORREO, "ana@unal.edu.co")
        escribir(CAMPO_CONTRASENA, "secreta1")
        tocar("Crear mi cuenta")

        compose.onNodeWithText("El nombre de usuario es obligatorio").assertExists()
        verify(exactly = 0) { auth.createUserWithEmailAndPassword(any(), any()) }
    }

    @Test
    fun registroConNombreDeEspacios_muestraError() {
        abrirRegistro()
        escribir(CAMPO_NOMBRE, "   ")
        escribir(CAMPO_CORREO, "ana@unal.edu.co")
        escribir(CAMPO_CONTRASENA, "secreta1")
        tocar("Crear mi cuenta")

        compose.onNodeWithText("El nombre de usuario es obligatorio").assertExists()
    }

    @Test
    fun registroConCorreoInvalido_muestraError() {
        abrirRegistro()
        escribir(CAMPO_NOMBRE, "Ana")
        escribir(CAMPO_CORREO, "ana@unal")
        escribir(CAMPO_CONTRASENA, "secreta1")
        tocar("Crear mi cuenta")

        compose.onNodeWithText("Correo electrónico inválido").assertExists()
        verify(exactly = 0) { auth.createUserWithEmailAndPassword(any(), any()) }
    }

    @Test
    fun registroValido_creaLaCuenta() {
        abrirRegistro()
        escribir(CAMPO_NOMBRE, "Ana")
        escribir(CAMPO_CORREO, "ana@unal.edu.co")
        escribir(CAMPO_CONTRASENA, "secreta1")
        tocar("Crear mi cuenta")

        verify(exactly = 1) { auth.createUserWithEmailAndPassword("ana@unal.edu.co", "secreta1") }
    }

    private companion object {
        const val CAMPO_NOMBRE = "Nombre de usuario"
        const val CAMPO_CORREO = "Correo institucional o personal"
        const val CAMPO_CONTRASENA = "Contraseña"
    }
}
