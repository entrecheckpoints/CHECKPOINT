# Checkpoint 1.0.1 - MagazineHome compile hotfix

Corrige el error de compilación:

`Unresolved reference 'matchParentSize'`

## Aplicación

Sube el contenido de esta carpeta a la raíz del repositorio y reemplaza el archivo existente.

Archivo modificado:

`app/src/main/java/com/entrecheckpoints/checkpoint/ui/MagazineHome.kt`

La corrección elimina el import inválido de `matchParentSize`. La función sigue disponible dentro de `BoxScope`, donde se utiliza en la pantalla de revista.
