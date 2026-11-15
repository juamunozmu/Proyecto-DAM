package com.unal.nachoquest.ui.screens.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.unal.nachoquest.R
import com.unal.nachoquest.ui.components.BotonJuego
import com.unal.nachoquest.ui.theme.CantoCrema
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.Tinta
import com.unal.nachoquest.ui.theme.TintaSuave
import kotlinx.coroutines.launch

private const val TAG = "AccesoConProveedores"

// Según la versión de Google Play Services, el flujo "Sign in with Google" responde con uno u otro tipo
private val TIPOS_CREDENCIAL_GOOGLE = setOf(
    GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL,
    GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL
)

/**
 * Separador "o continúa con" y botones de acceso con Google y Apple,
 * comunes a la pantalla de inicio de sesión y a la de registro.
 */
@Composable
fun AccesoConProveedores(viewModel: AuthViewModel, habilitado: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = CantoCrema)
        Text(
            text = "  o continúa con  ",
            style = MaterialTheme.typography.bodySmall,
            color = TintaSuave
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = CantoCrema)
    }

    Spacer(modifier = Modifier.height(18.dp))

    BotonJuego(
        texto = "Continuar con Google",
        onClick = {
            scope.launch {
                when (val resultado = pedirCuentaDeGoogle(context)) {
                    is AccesoGoogle.Token -> viewModel.iniciarSesionConGoogle(resultado.idToken)
                    is AccesoGoogle.Error -> viewModel.mostrarError(resultado.mensaje)
                    AccesoGoogle.Cancelado -> Unit
                }
            }
        },
        habilitado = habilitado,
        color = Color.White,
        colorTexto = Tinta,
        borde = CantoCrema,
        prefijo = "G",
        colorPrefijo = Color(0xFF4285F4),
        alto = 48.dp,
        tamanoTexto = 15.sp,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(10.dp))

    BotonJuego(
        texto = "Continuar con Apple",
        onClick = { context.buscarActividad()?.let(viewModel::iniciarSesionConApple) },
        habilitado = habilitado,
        color = Color.White,
        colorTexto = Tinta,
        borde = CantoCrema,
        alto = 48.dp,
        tamanoTexto = 15.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

/** Error (rojo) o aviso (verde) bajo los campos del formulario. */
@Composable
fun MensajesAcceso(error: String?, mensaje: String?) {
    val (texto, color) = when {
        error != null -> error to MaterialTheme.colorScheme.error
        mensaje != null -> mensaje to NachoGreen
        else -> return
    }
    Text(
        text = texto,
        color = color,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

private sealed interface AccesoGoogle {
    data class Token(val idToken: String) : AccesoGoogle
    data class Error(val mensaje: String) : AccesoGoogle
    data object Cancelado : AccesoGoogle
}

/** Muestra el selector de cuentas de Google (Credential Manager) y devuelve el token para Firebase. */
private suspend fun pedirCuentaDeGoogle(context: Context): AccesoGoogle {
    val opcion = GetSignInWithGoogleOption.Builder(context.getString(R.string.default_web_client_id)).build()
    val solicitud = GetCredentialRequest.Builder().addCredentialOption(opcion).build()
    return try {
        val credencial = CredentialManager.create(context).getCredential(context, solicitud).credential
        if (credencial is CustomCredential && credencial.type in TIPOS_CREDENCIAL_GOOGLE) {
            AccesoGoogle.Token(GoogleIdTokenCredential.createFrom(credencial.data).idToken)
        } else {
            AccesoGoogle.Error("Google devolvió una credencial que no reconocemos.")
        }
    } catch (e: GetCredentialCancellationException) {
        AccesoGoogle.Cancelado
    } catch (e: NoCredentialException) {
        AccesoGoogle.Error("No hay cuentas de Google disponibles en este dispositivo.")
    } catch (e: GetCredentialException) {
        Log.w(TAG, "No se pudo obtener la cuenta de Google", e)
        AccesoGoogle.Error("No se pudo iniciar sesión con Google. Inténtalo de nuevo.")
    } catch (e: GoogleIdTokenParsingException) {
        Log.w(TAG, "Token de Google inválido", e)
        AccesoGoogle.Error("No se pudo iniciar sesión con Google. Inténtalo de nuevo.")
    }
}

private tailrec fun Context.buscarActividad(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.buscarActividad()
    else -> null
}
