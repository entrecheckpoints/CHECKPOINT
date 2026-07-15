# Aplicar el parche Checkpoint Android 0.3.1

Esta actualización corrige temas, legibilidad, jerarquía visual y organización de la pantalla principal. No modifica la base de datos.

1. Exporta un respaldo desde Checkpoint por precaución.
2. Descomprime `checkpoint-android-v0.3.1-visual-harmony-patch.zip`.
3. Sube su contenido a la raíz de tu repositorio y permite reemplazar los archivos existentes.
4. Confirma el commit en la rama `main`.
5. Abre **Actions → Build Checkpoint APK**.
6. Espera a que las pruebas y la compilación terminen en verde.
7. Descarga el artefacto `checkpoint-android-v0.3.1-debug`.
8. Extrae e instala `app-debug.apk`.

Si Android rechaza la actualización por conflicto de firma, respalda los datos, desinstala el APK anterior, instala 0.3.1 e importa el respaldo. Las compilaciones generadas desde 0.3.0 por este repositorio deberían compartir la firma de desarrollo incluida.
