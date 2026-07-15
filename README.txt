CHECKPOINT 1.0.0 - HOTFIX DE COMPILACION

El archivo build.gradle.kts de la raiz del repositorio fue reemplazado accidentalmente por contenido YAML de GitHub Actions.

INSTRUCCIONES:
1. Sube build.gradle.kts a la RAIZ del repositorio.
2. Reemplaza el archivo existente.
3. No lo subas dentro de app/ ni dentro de .github/.
4. Confirma que .github/workflows/android.yml siga existiendo por separado.
5. Haz commit en main. GitHub Actions iniciara otra compilacion.

La estructura correcta en la raiz debe verse asi:

.github/
app/
build.gradle.kts
settings.gradle.kts
gradlew
gradlew.bat

Este hotfix no cambia la version de la app ni sus datos.
