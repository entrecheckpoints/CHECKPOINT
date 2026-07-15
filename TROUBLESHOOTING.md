# Solución de problemas de compilación

## Método recomendado

En Windows ejecuta:

```bat
scripts\build-apk.bat
```

El APK se genera en:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Este script solo compila el APK. Las pruebas se ejecutan por separado con `scripts\run-tests.bat`.

## Configuración requerida

- Android Studio reciente.
- JDK 17 o el JDK integrado de Android Studio.
- Android SDK Platform 35.
- Android SDK Build-Tools 35.x.
- Conexión a internet durante la primera sincronización para descargar Gradle y dependencias.

## Errores comunes

### SDK location not found

Abre el proyecto en Android Studio y deja que cree `local.properties`. También puedes crear el archivo en la raíz con:

```properties
sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk
```

### Failed to find target android-35

En Android Studio abre **Tools > SDK Manager** e instala **Android 15 / API 35** y sus Build-Tools.

### JAVA_HOME is set to an invalid directory

En Android Studio usa **File > Settings > Build, Execution, Deployment > Build Tools > Gradle** y selecciona **Gradle JDK: Embedded JDK / jbr-17 o jbr-21**.

### Could not find or load GradleWrapperMain

Vuelve a extraer el ZIP corregido. Debe existir `gradle/wrapper/gradle-wrapper.jar` y pesar aproximadamente 43 KB.

### Gradle download failed

Desactiva temporalmente VPN, proxy o antivirus que intercepte conexiones, y vuelve a sincronizar. Gradle 8.9 se descarga desde `services.gradle.org`.

## App not installed / package conflicts with an existing package

Los APK debug anteriores pudieron ser firmados con claves temporales distintas en GitHub Actions. Antes de desinstalar:

1. Exporta un respaldo JSON desde la app anterior.
2. Desinstala Checkpoint.
3. Instala 0.3.0.
4. Importa el respaldo.

Desde 0.3.0 el repositorio incluye `checkpoint-debug.keystore`, por lo que las siguientes compilaciones debug del mismo proyecto mantendrán la firma.

## No aparece el widget

Después de instalar o actualizar:

1. Abre Checkpoint al menos una vez.
2. Mantén pulsada la pantalla de inicio.
3. Abre **Widgets** y busca Checkpoint.
4. Si el launcher conserva una lista antigua, reinícialo o reinicia el teléfono.

## La wishlist de Steam no importa nada

La wishlist debe ser pública y su HTML debe exponer App IDs. Como alternativa, pega directamente los enlaces de cada juego o sus App IDs, uno por línea.
