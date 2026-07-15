# Aplicar el parche Checkpoint Android 1.0.0

1. Exporta un respaldo desde la versión actual por precaución.
2. Descomprime el parche 1.0.0.
3. Sube su contenido a la raíz del repositorio, reemplazando archivos existentes.
4. Confirma que `.github/workflows/android.yml` esté en la raíz correcta.
5. Haz commit en `main`.
6. Abre **Actions → Build Checkpoint APK**.
7. Descarga `checkpoint-android-v1.0.0-debug`.
8. Extrae e instala `app-debug.apk`.

El parche no cambia Room ni el formato JSON. Si Android rechaza la actualización por firma, exporta un respaldo, desinstala la versión anterior, instala 1.0.0 e importa el respaldo.
