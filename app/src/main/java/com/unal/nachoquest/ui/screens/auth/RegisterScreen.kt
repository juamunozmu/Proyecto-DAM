package com.unal.nachoquest.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.unal.nachoquest.domain.model.Expresion
import com.unal.nachoquest.ui.components.BotonJuego
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.Tinta

/**
 * Pantalla de registro de nuevo usuario.
 */
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegistroExitoso: () -> Unit,
    onIrALogin: () -> Unit
) {
    val state by viewModel.authState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.limpiarMensajes()
    }

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) {
            onRegistroExitoso()
        }
    }

    MarcoAcceso(
        titulo = "Únete a la aventura",
        subtitulo = "Crea tu cuenta y ayuda a Nacho a salvar la memoria del campus.",
        expresion = if (state.error != null) Expresion.PREOCUPADO else Expresion.FELIZ
    ) {
        CampoAcceso(
            valor = state.nombre,
            onValor = viewModel::onNombreChange,
            etiqueta = "Nombre de usuario",
            icono = Icons.Default.Person,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(14.dp))

        CampoAcceso(
            valor = state.email,
            onValor = viewModel::onEmailChange,
            etiqueta = "Correo institucional o personal",
            icono = Icons.Default.Email,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(14.dp))

        CampoAcceso(
            valor = state.password,
            onValor = viewModel::onPasswordChange,
            etiqueta = "Contraseña",
            icono = Icons.Default.Lock,
            esContrasena = true,
            ayuda = "Mínimo 6 caracteres",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { viewModel.register() })
        )

        MensajesAcceso(error = state.error, mensaje = state.mensaje)

        Spacer(modifier = Modifier.height(20.dp))

        BotonJuego(
            texto = "Crear mi cuenta",
            onClick = viewModel::register,
            cargando = state.isLoading,
            alto = 52.dp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(22.dp))

        AccesoConProveedores(viewModel = viewModel, habilitado = !state.isLoading)

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("¿Ya tienes una cuenta?", style = MaterialTheme.typography.bodyMedium, color = Tinta)
            TextButton(onClick = onIrALogin) {
                Text("Inicia sesión", color = NachoGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}
