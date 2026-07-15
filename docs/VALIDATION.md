# Validación técnica de Checkpoint Android 1.0.0

Validaciones preparadas para el paquete:

- Revisión de balance de llaves, paréntesis y corchetes en Kotlin.
- Comprobación de sintaxis con `kotlinc` sin errores de parseo.
- Validación de XML y YAML.
- Verificación de `versionName 1.0.0` y `versionCode 7`.
- Revisión del artefacto `checkpoint-android-v1.0.0-debug`.
- Verificación del workflow de GitHub Releases.
- Pruebas unitarias añadidas para la lógica editorial de la portada.
- Confirmación de que Room y el formato de respaldo no cambiaron.
- Comprobación de integridad de ZIP y SHA-256 después del empaquetado.

## Validación pendiente en GitHub Actions

Este entorno no dispone de Android SDK ni de las dependencias Gradle descargadas. La compilación Android completa debe ejecutarse en GitHub Actions o Android Studio. El workflow incluido instala SDK 35, ejecuta las pruebas y genera el APK.

## Checkpoint 1.0.3

- Parser local validado con enlaces Nintendo `/wish-list/share/`, región `es-mx`, SKU duplicados y fecha.
- Sintaxis Kotlin de `WishlistImporter` y `NintendoSharedWishlistResolver` comprobada con `kotlinc` y stubs Android.
- Sintaxis JavaScript del extractor WebView comprobada con Node.js.
- YAML de GitHub Actions validado.
- XML de Android validado.
- La compilación Android completa debe ejecutarse en GitHub Actions porque el entorno de generación no tiene acceso de red para descargar Gradle.
