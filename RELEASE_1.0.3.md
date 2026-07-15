# Checkpoint Android 1.0.3 — Dual Wishlist Import

## Nintendo Shared Wishlist

Checkpoint reconoce enlaces oficiales como:

```text
https://www.nintendo.com/es-mx/wish-list/share/#skus=...
```

El enlace se analiza localmente para obtener SKU, región, idioma y fecha. Después se abre la página pública en un WebView temporal para recuperar los enlaces de producto generados por el JavaScript de Nintendo.

## Steam Public Wishlist

Se conserva la importación paginada de wishlists públicas por SteamID, nombre personalizado y perfiles de Steam Community, además de App IDs, JSON y enlaces individuales.

## Interfaz y diagnóstico

- El diálogo distingue Steam, Nintendo y Xbox.
- Se indica que Nintendo puede tardar con listas grandes.
- El resultado muestra listas procesadas, productos importados, fallos y elementos ignorados.
- Errores específicos para enlaces sin SKU, WebView no disponible, tiempo agotado y páginas sin fichas.

## Compatibilidad

- `versionName`: 1.0.3
- `versionCode`: 9
- Room: sin cambios
- Respaldo JSON: sin cambios
- Firma debug fija: compatible con las compilaciones recientes del proyecto
