# Checkpoint Android 0.2.0 — Temas

## Cambios

- Selector de apariencia dentro de Ajustes.
- Cambio instantáneo sin reiniciar la actividad.
- Persistencia del tema con SharedPreferences.
- Exportación e importación del tema en respaldos JSON versión 4.
- Cuatro sistemas visuales: Editorial, After Dark, Frutiger Aero y Arcade Neon.
- Texturas programáticas sin depender de imágenes externas.
- Formas variables: editorial recto, oscuro sutil, Aero redondeado y Neon técnico.
- Saturación de portadas específica por tema.
- Colores de Nintendo, Steam y Xbox adaptados para mantener contraste.

## Migración

La base de datos y los juegos existentes no cambian. Al instalar esta versión sobre la anterior, Checkpoint inicia con Editorial hasta que el usuario elija otro tema. No se requiere borrar datos.
