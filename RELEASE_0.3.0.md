# Checkpoint Android 0.3.0 — Notas de publicación

## Resumen

Esta versión transforma Checkpoint de un rastreador de precios básico en una suite local para seguimiento, análisis, comparación, biblioteca y presupuesto. No incluye sincronización ni conexión con la extensión de Chrome.

## Antes de instalar

Si tienes una compilación debug anterior, exporta un respaldo. Desde 0.3.0 se utiliza una firma de desarrollo fija para que futuras compilaciones de GitHub Actions puedan actualizarse entre sí. La primera instalación podría requerir desinstalar un APK anterior firmado con otra clave.

## Datos conservados

La migración de Room mantiene:

- Juegos.
- Historial.
- Objetivos.
- Ajustes.
- Tema.

Las nuevas propiedades se inicializan con valores seguros y pueden editarse después desde cada ficha.

## Funciones nuevas

- Alertas inteligentes.
- Feed persistente de ofertas.
- Comparación multitienda.
- Deal Score y análisis histórico.
- Pronóstico heurístico.
- Importación masiva y wishlist pública de Steam.
- Biblioteca personal.
- Presupuesto mensual.
- Suscripciones.
- Widget Android.
- Diagnóstico de fuentes.
- Respaldo JSON ampliado.

Consulta `CHANGELOG.md` para el detalle completo.
