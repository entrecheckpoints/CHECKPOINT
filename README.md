# Checkpoint by Entre Checkpoints — Android 1.0.0

Checkpoint es una aplicación Android nativa para vigilar precios de videojuegos en **Nintendo eShop, Steam y Xbox Store**, comparar tiendas, administrar una biblioteca personal y recibir alertas inteligentes.

La versión 1.0 convierte el inicio en una **portada editorial dinámica** inspirada en las revistas de videojuegos de mediados de los 2000. No es una imagen estática: se construye con los juegos, precios, descuentos, objetivos, eventos e historial guardados en el teléfono.

Checkpoint funciona de manera local, no requiere cuentas, no incluye telemetría y no se sincroniza con la extensión de Chrome.

![Concepto de la portada dinámica de Checkpoint 1.0](docs/checkpoint-v1-magazine-concept.png)

## Checkpoint 1.0 — Live Magazine Edition

Al abrir la aplicación, Inicio muestra una portada viva con:

- Masthead `CHECKPOINT by ENTRE CHECKPOINTS`.
- Número de edición diario y código de barras generado localmente.
- Juego de portada elegido según Deal Score, eventos recientes, descuento y objetivos.
- Titulares automáticos como `NEW HISTORICAL LOW`, `TARGET REACHED`, `ENDING SOON` y `BEST DEAL TODAY`.
- Arte del juego cargado desde su ficha de tienda.
- Precio actual, precio anterior y porcentaje de descuento.
- Nuevas ofertas, objetivos alcanzados y ahorro disponible.
- Selección horizontal **Best Deals Today**.
- Panel **Price Pulse** con movimiento agregado de la lista.
- Acciones compactas para agregar juegos y actualizar precios.

La interfaz funcional continúa debajo de la portada: búsqueda, filtros y lista completa de seguimiento. Las demás secciones permanecen separadas para evitar convertir la portada en una cabina de avión con ansiedad.

## Secciones

- **Inicio:** portada dinámica, ofertas destacadas, Price Pulse y seguimiento.
- **Ofertas:** feed de bajadas, mínimos, objetivos y promociones activas.
- **Comparar:** precios del mismo juego y edición entre tiendas.
- **Biblioteca:** estado, compras, notas, formato, calificación y presupuesto.
- **Sistema:** temas, automatización, suscripciones, fuentes y respaldos.

## Funciones

### Seguimiento y alertas

- Agregar productos mediante URL.
- Recibir enlaces con **Compartir → Checkpoint** desde Android.
- Actualización manual y periódica con WorkManager.
- Alertas por bajada, objetivo, mínimo, descuento, cantidad reducida y fin de oferta.
- Conservación del último precio válido cuando una fuente falla.

### Análisis

- Precio mínimo, máximo y promedio local.
- Deal Score de 0 a 10.
- Gráfica histórica.
- Pronóstico heurístico de próximas ofertas.
- Comparación multitienda por juego y edición.

### Organización

- Wishlist e importación masiva.
- Biblioteca: deseado, comprado, jugando, terminado y abandonado.
- Precio pagado, fecha de compra, formato, calificación y notas.
- Presupuesto mensual y combinación sugerida de compras.
- Suscripciones: Game Pass, EA Play, Ubisoft+, PlayStation Plus y Nintendo Switch Online.

### Apariencia

- Editorial Y2K.
- After Dark.
- Frutiger Aero.
- Arcade Neon.

La portada de revista se adapta a cada tema, pero siempre utiliza una capa de contraste estable sobre el arte para mantener el precio y los titulares legibles.

## Actualizar desde 0.3.1

No se modificó el esquema de Room ni el formato de respaldo. Juegos, historial, eventos, objetivos, biblioteca, presupuesto y tema se conservan al instalar el APK con la misma firma.

La versión utiliza:

```text
versionName 1.0.0
versionCode 7
```

## Compilar en GitHub Actions

1. Sube el contenido del proyecto a la raíz del repositorio.
2. Comprueba que exista `.github/workflows/android.yml`.
3. Abre **Actions → Build Checkpoint APK**.
4. Pulsa **Run workflow** o realiza un commit en `main`.
5. Descarga el artefacto `checkpoint-android-v1.0.0-debug`.
6. Extrae `app-debug.apk`.

## Publicar el APK en GitHub Releases

El proyecto incluye `.github/workflows/release-apk.yml`.

1. Abre **Actions → Publicar Checkpoint APK**.
2. Pulsa **Run workflow**.
3. Escribe `v1.0.0`.
4. Al terminar, GitHub crea una Release con:
   - `Checkpoint.apk`
   - `Checkpoint.apk.sha256`

En el README público puedes utilizar:

```markdown
[Descargar Checkpoint](https://github.com/entrecheckpoints/CHECKPOINT/releases/latest/download/Checkpoint.apk)
```

El APK se firma con la clave de desarrollo incluida en el repositorio. Sirve para distribución directa y actualizaciones entre compilaciones de este proyecto, pero no debe utilizarse como firma definitiva para Google Play.

## Compilar localmente

Requisitos:

- Android Studio reciente.
- JDK 17.
- Android SDK 35.
- Build Tools 35.x.

Windows:

```bat
scripts\build-apk.bat
```

Linux o macOS:

```bash
./scripts/build-apk.sh
```

APK generado:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Respaldos

El JSON conserva juegos, historial, eventos, alertas, comparación, biblioteca, suscripciones, presupuesto, intervalo de actualización, notificaciones y tema.

## Limitaciones

- Xbox continúa en beta porque sus páginas públicas cambian con frecuencia.
- El historial representa los datos reunidos por Checkpoint, no el historial global de SteamDB.
- El pronóstico requiere varios ciclos reales de precio.
- Android puede retrasar tareas de WorkManager para ahorrar batería.
- El ahorro de portada usa una sola moneda, tomada del juego destacado, para no sumar pesos, dólares y yenes como si fueran fichas del mismo arcade.

Consulta [CHANGELOG.md](CHANGELOG.md) para el detalle completo de versiones.
