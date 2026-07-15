# Patch 0.1.2

Errores corregidos en `CheckpointScreen.kt`:

1. Eliminado:
   `import androidx.compose.foundation.layout.weight`
2. Sustituido:
   `import androidx.compose.foundation.shape.RectangleShape`
   por:
   `import androidx.compose.ui.graphics.RectangleShape`

El primer import apuntaba a una propiedad interna de Compose. El segundo paquete no contiene `RectangleShape`; la clase está en `androidx.compose.ui.graphics`.
