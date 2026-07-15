# Aplicar el parche Checkpoint Android 0.3.0

El paquete de parche conserva la misma estructura que la raíz del repositorio.

## Desde GitHub web

1. Descomprime `checkpoint-android-v0.3.0-utility-patch.zip`.
2. Abre la raíz de tu repositorio.
3. Selecciona **Add file → Upload files**.
4. Arrastra todos los archivos y carpetas del parche.
5. Confirma que GitHub muestre los archivos como agregados o reemplazados.
6. Pulsa **Commit changes** en la rama `main`.
7. Abre **Actions → Build Checkpoint APK**.
8. Al terminar, descarga el artefacto `checkpoint-android-v0.3.0-debug`.

## Firma de actualización

El parche añade `checkpoint-debug.keystore` y configura el APK debug para utilizarlo. Es una clave pública de desarrollo destinada a mantener una firma estable en futuras compilaciones del repositorio.

Un APK anterior podría tener una firma distinta. Exporta un respaldo antes de desinstalarlo. No utilices esta clave para una publicación de producción.
