# Changelog de Checkpoint Android

Este documento registra los cambios funcionales y técnicos de cada versión. El proyecto no incluye sincronización con la extensión de Chrome.

## 1.0.3 — Dual Wishlist Import

### Wishlist compartida de Nintendo

- Se añadió reconocimiento directo de enlaces oficiales con la ruta `/wish-list/share/`.
- El importador extrae del fragmento de la URL los SKU, la región, el idioma y la fecha incluida por Nintendo.
- Soporte para listas compartidas de México y para otras rutas regionales con formato `idioma-país`.
- Los SKU se deduplican conservando el orden original de la lista.
- Límite preventivo de 500 SKU por enlace compartido.
- Nuevo `NintendoSharedWishlistResolver`, que abre temporalmente la página pública en un WebView aislado para permitir que el JavaScript oficial hidrate los productos.
- El resolver recorre la página, activa la carga diferida y recupera enlaces de producto visibles o incrustados en el documento.
- La importación no requiere credenciales de Nintendo porque trabaja con la página compartida por el usuario.
- El WebView se destruye al terminar, al cancelar o al alcanzar el tiempo límite.
- Mensajes específicos para enlaces sin SKU, listas vencidas, errores de red y páginas que no exponen productos.

### Wishlist pública de Steam

- Se conserva la lectura paginada del endpoint público `wishlistdata`.
- Se mantienen URLs por SteamID, nombres personalizados y perfiles de Steam Community.
- Se conservan App IDs, JSON copiado y enlaces individuales como métodos alternativos.
- La detección de Steam se ejecuta antes que la detección de enlaces de producto para evitar tratar una lista como un solo juego.

### Importación y experiencia

- El diálogo ahora identifica explícitamente wishlists de Steam y Nintendo.
- Se advierte que Nintendo puede tardar mientras la página pública carga todos los productos.
- El resumen indica cuántas listas fueron procesadas.
- `líneas ignoradas` se sustituyó por `elementos ignorados`, porque una wishlist no es exactamente una línea y el lenguaje también merece conservar algo de dignidad.
- Xbox permanece limitado a enlaces individuales.

### Pruebas y mantenimiento

- Nuevas pruebas para región, SKU duplicados, fecha de la lista y enlaces compartidos sin SKU.
- Nuevas pruebas para normalización de URLs de Nintendo sin eliminar el parámetro `sku`.
- User-Agent actualizado a Checkpoint 1.0.3.
- Versión actualizada a `1.0.3` y código `9`.
- Artefacto de GitHub Actions actualizado a `checkpoint-android-v1.0.3-debug`.
- Workflow de Releases actualizado a `v1.0.3`.
- No se modificó el esquema de Room ni el formato de respaldo.

## 1.0.2 — Steam Wishlist Import Fix

- La importación dejó de depender únicamente de los App IDs presentes en el HTML inicial.
- Se añadió consulta paginada a `wishlistdata` para wishlists públicas de Steam.
- Compatibilidad con URLs `/wishlist/profiles/<SteamID>/` y `/wishlist/id/<usuario>/`.
- Conversión automática de perfiles públicos de Steam Community a su wishlist equivalente.
- Lectura flexible de JSON antiguo, JSON anidado, atributos `appid`, enlaces y claves numéricas.
- Límite preventivo de 2,000 productos.
- Mejor detalle del primer error encontrado durante una importación parcial.
- Versión actualizada a `1.0.2` y código `8`.

## 1.0.1 — Build Hotfix

- Se restauró el archivo raíz `build.gradle.kts` después de que contenido YAML del workflow terminara dentro de él.
- Se eliminó el import inválido `androidx.compose.foundation.layout.matchParentSize` de `MagazineHome.kt`.
- Se conservaron las llamadas válidas a `Modifier.matchParentSize()` dentro de `BoxScope`.
- No se modificaron funciones, datos ni diseño de la portada.

## 1.0.0 — Live Magazine Edition

### Portada editorial dinámica

- Inicio fue rediseñado como una portada de revista de videojuegos inspirada en la era Xbox 360 y mediados de los 2000.
- La portada se genera con datos reales de la base local; no utiliza una imagen estática como interfaz.
- Nuevo masthead `CHECKPOINT by ENTRE CHECKPOINTS`.
- Número de edición calculado según el día del año.
- Fecha editorial y código de barras generados automáticamente.
- El juego de portada se elige mediante Deal Score, descuento, objetivos y eventos recientes.
- Los eventos solo reciben prioridad editorial durante siete días para evitar que una noticia antigua secuestre eternamente la portada.
- Titulares automáticos para nuevo mínimo, objetivo alcanzado, oferta próxima a terminar, oferta recuperada, gran descuento y mejor oportunidad.
- Arte de portada cargado desde la ficha del juego con saturación adaptada al tema.
- Degradados de contraste independientes del tema para proteger la legibilidad del precio y del titular.
- Color de acento según Nintendo, Steam o Xbox con cálculo de texto blanco o negro legible.

### Resumen diario

- Contador de nuevas ofertas no vistas.
- Contador de objetivos alcanzados.
- Ahorro disponible calculado únicamente dentro de la moneda del juego destacado.
- Indicador de juegos incluidos en suscripciones activas.
- Acciones de agregar y actualizar integradas discretamente en el masthead.

### Best Deals Today

- Nuevo carrusel editorial de hasta seis juegos.
- Ordenamiento mediante Deal Score.
- Portada, descuento, precio y puntaje visibles sin saturar la ficha principal.
- Acceso directo al feed completo de ofertas.

### Price Pulse

- Nuevo panel de movimiento agregado.
- Conteo de juegos que bajaron, subieron o permanecieron estables.
- Indicador de errores de fuente.
- Gráfica normalizada basada en la relación entre precio actual y precio regular, evitando mezclar monedas directamente.

### Navegación y jerarquía

- La sección Seguimiento pasa a llamarse Inicio.
- El icono principal cambia a Home.
- La cabecera tradicional se oculta en Inicio para que la portada sea la primera experiencia visible.
- Las demás secciones conservan su cabecera funcional.
- Búsqueda, filtros y lista de seguimiento permanecen debajo de la portada.

### Publicación

- Versión actualizada a `1.0.0` y código `7`.
- Artefacto de GitHub Actions actualizado a `checkpoint-android-v1.0.0-debug`.
- Nuevo workflow manual `release-apk.yml` para publicar `Checkpoint.apk` y su SHA-256 en GitHub Releases.
- User-Agent actualizado a Checkpoint 1.0.0.

### Pruebas y compatibilidad

- Nuevas pruebas para selección de titular, movimiento de precios y generación de edición diaria.
- No se modificó el esquema Room.
- No se modificó el formato de respaldo.
- Se conserva compatibilidad de actualización con 0.3.0 y 0.3.1 cuando utilizan la misma firma.

## 0.3.1 — Visual Harmony

### Legibilidad y accesibilidad

- Se recalibraron las cuatro paletas para conservar contraste legible entre texto, superficies y botones.
- Se separaron los colores `onAccent` y `onAccentSecondary`; los botones secundarios ya no reutilizan a ciegas el color de texto del acento principal.
- Se añadieron tokens para superficie elevada, borde fuerte, éxito y advertencia.
- Frutiger Aero utiliza paneles opacos en lugar de transparencias que mezclaban texto con cielo y burbujas.
- After Dark usa un gris carbón escalonado y texto blanco roto para reducir fatiga visual.
- Arcade Neon limita el resplandor a acentos y gráficas; el texto normal permanece sobre superficies oscuras estables.
- Los badges de tienda y estado ahora utilizan texto neutro de alto contraste acompañado por un indicador de color.
- Se añadió `ThemeContrastTest`, que valida una relación mínima de 7:1 para texto principal y 4.5:1 para texto secundario y botones.

### Recuperación de la identidad Y2K editorial

- Se recuperó el fondo crema, la tinta seca, la lavanda y la geometría recta como lenguaje predeterminado.
- La retícula de papel es más amplia y tenue; deja de competir con tarjetas y párrafos.
- Nueva cabecera tipo masthead con código de sistema, número de edición, código de barras y subtítulo **by Entre Checkpoints**.
- Los códigos de barras se reservan para la cabecera y momentos de identidad, en lugar de repetirse debajo de cada título.
- Los encabezados de sección usan una sola regla editorial y un código técnico discreto.

### Pantalla principal

- Nuevo panel hero **Price Watch / Live**.
- La portada muestra el producto con mejor oportunidad según Deal Score y descuento.
- Las métricas de ofertas, objetivos y suscripciones se integran debajo del hero.
- El total seguido se muestra como dato editorial y no como una cuarta columna comprimida.
- Búsqueda y filtros se agrupan dentro de un solo panel.
- Las tarjetas de juego reducen badges simultáneos y muestran únicamente el estado contextual más importante.
- Fecha de revisión y actualización individual se integran en una cabecera compacta.

### Navegación y jerarquía

- La barra inferior dejó de ser una fila desplazable de botones delineados.
- Las cinco secciones ahora ocupan posiciones fijas con icono, etiqueta corta e indicador de selección.
- El aviso de eventos no vistos se representa mediante un punto discreto.
- La importación masiva se retiró de la cabecera principal y permanece disponible en Sistema, donde corresponde.
- Paneles de eventos, comparaciones, biblioteca, presupuesto, fuentes y ajustes comparten la misma jerarquía de superficie.

### Ficha de juego

- Alertas, comparación, biblioteca, suscripciones y notas se convirtieron en bloques plegables.
- La ficha abre mostrando únicamente precio, análisis, historial y objetivo.
- Cada bloque avanzado incluye un resumen antes de expandirse.
- Se redujo el número de bordes negros fuertes y botones visibles al mismo tiempo.

### Ajustes y temas

- El selector de temas se organiza en una cuadrícula de dos columnas.
- Cada opción muestra una vista previa con los colores reales del tema, no con los del tema actualmente activo.
- Las acciones Wishlist e Importar comparten una fila; Exportar respaldo ocupa una acción principal independiente.

### Compilación y mantenimiento

- Versión actualizada a `0.3.1` y código `6`.
- Artefacto de GitHub Actions actualizado a `checkpoint-android-v0.3.1-debug`.
- No se modificó el esquema de Room ni el formato de respaldo.
- Se conserva compatibilidad de actualización con 0.3.0 usando la misma firma de desarrollo.

## 0.3.0 — Utility Suite

### Alertas inteligentes

- Se añadió una configuración independiente de alertas por juego.
- Nueva alerta para cualquier bajada de precio.
- Nueva alerta por precio objetivo alcanzado.
- Nueva alerta por nuevo mínimo histórico local.
- Nueva alerta por porcentaje mínimo de descuento.
- Nueva alerta por una bajada mínima expresada en dinero.
- Nueva alerta por oferta próxima a terminar cuando existe una fecha publicada.
- Detección del regreso de una oferta después de volver temporalmente al precio normal.
- Se agregó prioridad de eventos para producir mensajes de notificación más relevantes.

### Feed de ofertas y eventos

- Nueva entidad Room `game_events`.
- Registro persistente de bajadas, mínimos, objetivos, descuentos, ofertas recuperadas, ofertas próximas a terminar y errores.
- Nueva sección **Ofertas** con eventos ordenados por fecha.
- Indicador de eventos no vistos en la navegación.
- Acción automática para marcar eventos como vistos al entrar a la sección.
- Los eventos se incluyen en exportaciones e importaciones JSON.

### Comparación multitienda

- Normalización automática de títulos para generar una clave de comparación.
- Detección inicial de ediciones Base, Deluxe, Ultimate, Complete, Remake/Remaster, Bundle y DLC.
- Nueva sección **Comparar**.
- Agrupación solamente cuando coinciden la clave normalizada y la edición.
- Identificación visual de la tienda más barata.
- Cálculo de ahorro frente a las demás tiendas del grupo.
- Edición manual de la clave de comparación y la edición desde la ficha del juego.
- Índice Room para acelerar consultas por clave de comparación.

### Historial avanzado y Deal Score

- Cálculo de precio mínimo, máximo y promedio registrado.
- Cálculo de ahorro respecto al precio regular.
- Nuevo Deal Score local de 0 a 10.
- El puntaje combina descuento actual, relación con el mínimo registrado y posición frente al promedio.
- Gráfica histórica integrada en la ficha detallada.
- Resumen analítico visible en las tarjetas y la ficha.
- Preferencia de historial ampliada y recorte configurable para evitar crecimiento ilimitado.

### Pronóstico de ofertas

- Detección de inicios de rebajas a partir del historial local.
- Cálculo del intervalo promedio entre ofertas.
- Estimación de una próxima ventana de rebaja cuando existe una muestra suficiente.
- Mensaje explícito cuando todavía no hay datos suficientes.
- El pronóstico se identifica como heurístico y no como fecha garantizada.

### Importación masiva

- Nuevo importador de múltiples enlaces separados por líneas.
- Compatibilidad con App IDs numéricos de Steam.
- Lectura de wishlists públicas de Steam cuando la página expone App IDs.
- Corrección del orden de detección para tratar una URL de wishlist como lista y no como página de producto.
- Detección automática de Nintendo, Steam y Xbox para cada elemento.
- Resultado detallado: agregados, actualizados, fallidos, ignorados y listas procesadas.
- Nuevo diálogo de importación masiva accesible desde la cabecera y Sistema.

### Biblioteca personal

- Nuevos estados: deseado, comprado, jugando, terminado y abandonado.
- Registro opcional de la tienda donde se posee el juego.
- Registro del precio pagado.
- Registro de fecha de compra.
- Selección de formato físico o digital.
- Calificación personal de 1 a 10.
- Notas privadas por juego.
- Nueva sección **Biblioteca** con filtros por estado.
- Los metadatos de biblioteca se incluyen en los respaldos.

### Presupuesto mensual

- Nueva preferencia de presupuesto mensual.
- Cálculo del gasto del mes con base en las compras registradas.
- Cálculo del saldo restante.
- Recomendación de una combinación de juegos dentro del presupuesto.
- La recomendación considera precio, ahorro y Deal Score.
- El presupuesto se exporta e importa con el respaldo.
- Los cálculos se limitan a la moneda predominante para evitar sumas entre divisas incompatibles.

### Suscripciones

- Servicios disponibles: Xbox Game Pass, EA Play, Ubisoft+, PlayStation Plus y Nintendo Switch Online.
- Configuración de las suscripciones activas del usuario.
- Etiquetas de disponibilidad por juego.
- Resaltado de productos incluidos en un servicio activo.
- Edición manual desde la ficha para corregir o completar información.
- Detección prudente desde páginas públicas cuando existe una señal clara.
- Datos de suscripción incluidos en respaldos.

### Widget Android

- Nuevo `AppWidgetProvider`.
- Diseño propio mediante `RemoteViews`.
- Muestra hasta tres ofertas u objetivos destacados.
- Actualización del widget después de sincronizaciones, cambios de juegos e importaciones.
- Acceso a la app al pulsar el widget.
- Botón **SYNC** para iniciar una revisión global desde la pantalla de inicio.
- Recursos y metadatos de widget incorporados al manifiesto.

### Diagnóstico de fuentes

- Nueva entidad Room `sync_runs`.
- Registro de sincronizaciones globales con duración, actualizados y errores.
- Panel por tienda con número de productos, errores y última revisión.
- Registro de errores como eventos del feed.
- Conservación del último precio válido cuando una actualización falla.

### Respaldo y migración

- Formato de respaldo actualizado a la versión 5.
- Exportación de eventos, biblioteca, comparación, alertas, presupuesto, suscripciones y fin de oferta.
- Compatibilidad de importación con respaldos previos.
- Base de datos Room actualizada de versión 1 a versión 2.
- Migración no destructiva que agrega nuevas columnas, tablas e índices.
- Se eliminó cualquier dependencia funcional de una conexión con la extensión de Chrome.
- Se conserva importación y exportación manual de archivos JSON locales.

### Interfaz

- Navegación principal con Seguimiento, Ofertas, Comparar, Biblioteca y Sistema.
- Nuevas tarjetas de métricas, eventos, comparaciones, presupuesto y estado de fuentes.
- Ficha de juego ampliada con análisis, reglas, biblioteca, suscripciones y notas.
- Temas Editorial, After Dark, Frutiger Aero y Arcade Neon aplicados a las nuevas pantallas.
- Se mantuvo el subtítulo **Checkpoint by Entre Checkpoints**.

### Red y parsers

- `ProductSnapshot` ahora admite fecha de finalización y suscripciones detectadas.
- Steam combina su respuesta de producto con HTML cuando necesita enriquecer fecha o servicios.
- Xbox intenta extraer fecha de finalización y etiquetas de servicios desde datos públicos.
- Nintendo conserva una estrategia prudente y no presupone inclusión en Nintendo Switch Online.
- User-Agent actualizado a Checkpoint 0.3.

### Compilación y mantenimiento

- Versión actualizada a `0.3.0` y código `5`.
- Nuevo artefacto de GitHub Actions: `checkpoint-android-v0.3.0-debug`.
- Pruebas unitarias para normalización de títulos y Deal Score.
- Inclusión de una clave de depuración fija para conservar la firma entre ejecuciones de GitHub Actions.
- La clave incluida es exclusivamente de desarrollo y no debe utilizarse para publicar en Google Play.
- README reescrito con funciones, migración, limitaciones y guía de compilación.

## 0.2.0 — Temas

- Se añadió el selector de apariencia en Ajustes.
- Nuevo tema Editorial.
- Nuevo tema After Dark.
- Nuevo tema Frutiger Aero.
- Nuevo tema Arcade Neon.
- Cambio instantáneo sin reiniciar la aplicación.
- Persistencia local del tema.
- Inclusión del tema seleccionado en los respaldos JSON.
- Adaptación de fondos, paletas, superficies, bordes, gráficas y saturación de portadas.

## 0.1.2 — Corrección de Compose

- Se eliminó el import incorrecto de `androidx.compose.foundation.layout.weight`.
- Se corrigió `RectangleShape` para utilizar `androidx.compose.ui.graphics.RectangleShape`.
- Se resolvieron errores de compilación de `CheckpointScreen.kt` detectados en GitHub Actions.

## 0.1.1 — Reparación del sistema de compilación

- Se reemplazó el Gradle Wrapper recortado por el wrapper oficial completo.
- Se separó la compilación del APK de la ejecución de pruebas.
- Se añadieron scripts específicos `build-apk` y `run-tests`.
- Se amplió la guía de solución de problemas.

## 0.1.0 — Primera versión Android

- Aplicación nativa en Kotlin y Jetpack Compose.
- Base local con Room.
- Seguimiento de Nintendo eShop, Steam y Xbox Store beta.
- Agregar juegos mediante URL o la hoja Compartir de Android.
- Actualización manual y periódica con WorkManager.
- Precio actual, precio normal, descuento, mínimo y objetivo.
- Historial local básico.
- Notificaciones por bajada y objetivo.
- Importación y exportación JSON.
- Enlace a SteamDB para productos de Steam.
- Primera identidad editorial de Checkpoint by Entre Checkpoints.
