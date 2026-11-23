# NachoQuest - Reglas de ProGuard / R8 para el release

# Modelos de datos: Firestore los arma por reflexión (toObject) a partir de los nombres de campo
-keep class com.unal.nachoquest.domain.model.** { *; }

# Firebase
-keepattributes Signature
-keepattributes *Annotation*

# Rutas de navegación: Navigation Compose las serializa con kotlinx.serialization
-keep class com.unal.nachoquest.ui.navigation.** { *; }
-keepattributes InnerClasses

# Credential Manager (acceso con Google): carga el proveedor de Play Services por reflexión
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** { *; }

# Para poder leer los errores de producción con el archivo mapping.txt
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
