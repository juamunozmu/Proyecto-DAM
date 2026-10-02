# NachoQuest - Reglas de ProGuard
# Agregar reglas específicas del proyecto aquí

# Mantener modelos de datos
-keep class com.unal.nachoquest.domain.model.** { *; }

# Firebase
-keepattributes Signature
-keepattributes *Annotation*
