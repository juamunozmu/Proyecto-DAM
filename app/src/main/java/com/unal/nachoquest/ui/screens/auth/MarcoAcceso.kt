package com.unal.nachoquest.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.unal.nachoquest.domain.model.Expresion
import com.unal.nachoquest.ui.components.Nacho
import com.unal.nachoquest.ui.components.ParticulasFlotantes
import com.unal.nachoquest.ui.theme.CantoCrema
import com.unal.nachoquest.ui.theme.NachoCream
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.TintaSuave

private val ALTO_CABECERA = 176.dp

/**
 * Marco común del inicio de sesión y del registro: Nacho saluda desde la cabecera verde y el
 * formulario va en una hoja crema que ocupa el resto de la pantalla.
 */
@Composable
fun MarcoAcceso(
    titulo: String,
    subtitulo: String,
    expresion: Expresion = Expresion.FELIZ,
    contenido: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NachoGreenDark, NachoGreen)))
    ) {
        val altoPantalla = maxHeight
        Box(modifier = Modifier.fillMaxWidth().height(ALTO_CABECERA)) {
            ParticulasFlotantes(modifier = Modifier.fillMaxSize(), cantidad = 14)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ALTO_CABECERA)
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Nacho(modifier = Modifier.size(96.dp), expresion = expresion)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = titulo, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitulo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            // La hoja llega siempre hasta el borde inferior, aunque el formulario sea corto
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = altoPantalla - ALTO_CABECERA)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(NachoCream)
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = contenido
            )
        }
    }
}

/**
 * Campo de texto del formulario de acceso. Si es de contraseña, trae el botón para mostrarla.
 *
 * @param ayuda indicación breve bajo el campo (p. ej. el largo mínimo de la contraseña)
 */
@Composable
fun CampoAcceso(
    valor: String,
    onValor: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    esContrasena: Boolean = false,
    ayuda: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = valor,
        onValueChange = onValor,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = if (esContrasena) {
            {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            }
        } else {
            null
        },
        supportingText = ayuda?.let { { Text(it, color = TintaSuave) } },
        visualTransformation = if (esContrasena && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = NachoGreen,
            unfocusedBorderColor = CantoCrema,
            focusedLabelColor = NachoGreen,
            unfocusedLabelColor = TintaSuave,
            focusedLeadingIconColor = NachoGreen,
            unfocusedLeadingIconColor = TintaSuave,
            cursorColor = NachoGreen
        )
    )
}
