# Parche Checkpoint Android 1.0.3

## Instalación

1. Descomprime `checkpoint-android-v1.0.3-dual-wishlist-patch.zip`.
2. Entra en la carpeta descomprimida.
3. Sube **su contenido** a la raíz del repositorio.
4. Reemplaza los archivos existentes.
5. Haz commit en `main`.
6. Abre **Actions → Build Checkpoint APK**.
7. Descarga `checkpoint-android-v1.0.3-debug`.

La raíz debe conservar esta estructura:

```text
.github/
app/
gradle/
build.gradle.kts
settings.gradle.kts
gradlew
```

## Prueba recomendada

Pega en **Sistema → Importación masiva** un enlace oficial de Nintendo con:

```text
/wish-list/share/#skus=
```

Mantén Checkpoint abierto mientras la página compartida carga los productos. El resultado debe indicar una lista procesada y el número de juegos añadidos, actualizados, fallidos o ignorados.

## Compatibilidad

- No cambia Room.
- No cambia el respaldo JSON.
- Conserva la firma debug fija del proyecto.
- Requiere Android System WebView habilitado para importar listas compartidas de Nintendo.
