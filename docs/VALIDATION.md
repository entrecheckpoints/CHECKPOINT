# Validación técnica de Checkpoint Android 0.3.1

Validaciones realizadas sobre el paquete antes de distribuirlo:

- Balance de llaves, paréntesis y corchetes en todos los archivos Kotlin.
- Comprobación sintáctica con el parser de `kotlinc`; no se detectaron errores de sintaxis.
- Validación de `AndroidManifest.xml` y recursos XML.
- Verificación de `versionName 0.3.1` y `versionCode 6`.
- Revisión de rutas del workflow y nombre del artefacto `checkpoint-android-v0.3.1-debug`.
- Cálculo independiente de contraste para texto principal, texto secundario, acentos y colores de tienda.
- Prueba unitaria `ThemeContrastTest` incluida para repetir la validación durante GitHub Actions.
- Verificación de que no se modificó la versión de Room ni el formato de respaldo.
- Prueba de integridad del ZIP final después del empaquetado.

## Limitación del entorno

El entorno donde se preparó el parche no dispone de Android SDK ni acceso de Gradle a internet. Por esa razón, la compilación Android completa debe ejecutarse con GitHub Actions o Android Studio. El workflow incluido instala SDK 35, ejecuta pruebas y genera el APK.
