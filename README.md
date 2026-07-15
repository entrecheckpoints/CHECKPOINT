# Checkpoint by Entre Checkpoints — Android 0.3.1

Aplicación Android nativa para vigilar precios de videojuegos, comparar tiendas, administrar una biblioteca personal y decidir si una oferta es realmente buena o solamente tiene un porcentaje rojo muy convincente.

Checkpoint funciona de manera local con **Nintendo eShop, Steam y Xbox Store**. No requiere cuentas, no incluye telemetría y **no se conecta ni sincroniza con la extensión de Chrome**.

## Novedades de 0.3.1 — Visual Harmony

Esta versión corrige legibilidad y jerarquía visual sin eliminar ninguna función de 0.3.0. La interfaz vuelve a priorizar la identidad editorial Y2K de **Entre Checkpoints**, pero con menos ruido y una navegación más clara.

- Paletas recalibradas con contraste mínimo AA para texto normal.
- Superficies opacas en Frutiger Aero para que el fondo no compita con la información.
- Brillos y retículas reducidos en After Dark y Arcade Neon.
- Tema Editorial recuperado como diseño principal: papel crema, tinta, lavanda, códigos técnicos y geometría recta.
- Nueva portada principal con una oferta destacada, métricas compactas y una jerarquía más editorial.
- Tarjetas de juegos simplificadas: una sola línea contextual en lugar de varias etiquetas compitiendo.
- Barra inferior fija, sin carrusel de botones ni desplazamiento horizontal.
- Selector de temas en cuadrícula con una vista previa real de cada paleta.
- Ficha de juego reorganizada: alertas, comparación, biblioteca, suscripciones y notas permanecen plegadas hasta que se necesitan.
- Acciones de respaldo reorganizadas para evitar tres botones apretados en la misma fila.
- Prueba automática de contraste añadida al workflow de GitHub Actions.

La actualización no cambia el esquema de Room ni el formato de respaldo. Se instala sobre 0.3.0 cuando ambas versiones utilizan la misma firma.

## Funciones principales de la serie 0.3

### Alertas inteligentes

Cada juego puede tener reglas independientes:

- Cualquier bajada de precio.
- Precio objetivo alcanzado.
- Nuevo mínimo registrado.
- Descuento mínimo configurable.
- Bajada mínima expresada en dinero.
- Oferta próxima a terminar, cuando la tienda publica una fecha.
- Regreso de una oferta que ya había terminado.

Los avisos se guardan además en un feed local para que no dependas únicamente de haber visto la notificación.

### Comparador multitienda

Checkpoint agrupa automáticamente productos que parecen corresponder al mismo juego y a la misma edición. La clave de comparación y la edición pueden corregirse manualmente desde la ficha.

Se distinguen, entre otras:

- Juego base.
- Deluxe, Ultimate y Complete.
- Remake o remaster.
- Bundle.
- DLC.

Dentro de cada grupo se muestra la tienda más barata y el ahorro frente a las demás opciones. La comparación es local: solamente utiliza productos que tú hayas agregado.

### Historial y análisis de ofertas

La ficha de cada juego incorpora:

- Precio mínimo, máximo y promedio registrado.
- Ahorro respecto al precio normal.
- **Deal Score** de 0 a 10.
- Gráfica del historial local.
- Estimación de ciclo de ofertas.
- Fecha aproximada de una posible nueva rebaja cuando existe suficiente historial.

El pronóstico es una heurística basada en tus propios registros. No conoce los planes secretos de las tiendas, por mucho que el numerito parezca profesional.

### Importación masiva de wishlist

El importador acepta:

- Varios enlaces de Nintendo, Steam o Xbox, uno por línea.
- App IDs de Steam.
- Una wishlist pública de Steam cuando la página permite leer sus identificadores.

La app reporta cuántos productos fueron agregados, actualizados, ignorados o no pudieron procesarse.

### Biblioteca personal

Cada producto puede marcarse como:

- Deseado.
- Comprado.
- Jugando.
- Terminado.
- Abandonado.

También admite:

- Tienda de propiedad.
- Precio pagado.
- Fecha de compra.
- Formato físico o digital.
- Calificación personal de 1 a 10.
- Notas privadas.

### Presupuesto mensual

Checkpoint calcula cuánto has gastado durante el mes con base en tus compras registradas, cuánto presupuesto queda y una combinación sugerida de juegos que cabe dentro del saldo disponible.

La recomendación prioriza oportunidades con mejor ahorro y Deal Score. No impide que compres otro juego para el backlog, porque una app todavía no tiene esa clase de autoridad moral.

### Suscripciones

Puedes indicar qué servicios tienes activos y marcar manualmente los juegos incluidos en:

- Xbox Game Pass.
- EA Play.
- Ubisoft+.
- PlayStation Plus.
- Nintendo Switch Online.

Checkpoint destaca coincidencias entre tus suscripciones activas y los juegos seguidos. La detección automática solo se aplica cuando la página pública ofrece una señal suficientemente clara; la edición manual es la fuente principal para evitar falsos positivos.

### Feed de ofertas

La sección **Ofertas** reúne eventos como:

- Bajadas de precio.
- Nuevos mínimos.
- Objetivos alcanzados.
- Descuentos configurados.
- Ofertas recuperadas.
- Ofertas próximas a terminar.
- Errores de fuente.

Los eventos nuevos se marcan como pendientes hasta abrir la sección.

### Widget de Android

El widget para la pantalla de inicio muestra hasta tres ofertas u objetivos destacados con el último precio conocido e incluye un botón **SYNC** para solicitar una revisión inmediata.

Para agregarlo:

```text
Mantener pulsada la pantalla de inicio
→ Widgets
→ Checkpoint
```

### Diagnóstico de fuentes

La sección **Sistema** muestra el estado de Nintendo, Steam y Xbox:

- Productos seguidos por tienda.
- Errores actuales.
- Última revisión conocida.
- Resultado de la sincronización global más reciente.

Cuando una consulta falla, Checkpoint conserva el último precio válido y registra el problema en lugar de reemplazarlo con basura, una cortesía que algunas aplicaciones todavía consideran opcional.

## Temas visuales

En **Sistema → Apariencia** puedes cambiar el estilo sin reiniciar:

- **Editorial:** crema, tinta negra, lavanda y composición de revista tecnológica Y2K.
- **After Dark:** carbón, blanco roto y lavanda eléctrica.
- **Frutiger Aero:** cielo, agua, verde, paneles claros y formas suaves.
- **Arcade Neon:** azul negro, cian, magenta y cuadrícula luminosa.

La selección se conserva en el dispositivo y dentro de los respaldos JSON.

## Funciones base

- Agregar productos pegando un enlace.
- Recibir enlaces con **Compartir → Checkpoint** desde el navegador.
- Actualización manual individual y global.
- Revisiones automáticas con WorkManager cada 1, 6, 12, 24 o 48 horas.
- Precio actual, precio normal, descuento, mínimo y objetivo.
- Historial local mediante Room.
- Notificaciones nativas.
- Exportación e importación de respaldos JSON.
- Enlace directo a SteamDB para productos de Steam.
- Datos locales, sin cuentas y sin conexión con la extensión de Chrome.

## Actualizar desde 0.2.0

La base de datos incluye una migración Room de la versión 1 a la 2. Juegos, precios, historial y configuración existentes se conservan al instalar una actualización firmada con la misma clave.

### Importante sobre los APK de GitHub Actions

Desde 0.3.0 el proyecto incluye una **clave de depuración fija** llamada `checkpoint-debug.keystore`. Su única finalidad es permitir que futuros APK de desarrollo generados en GitHub Actions puedan instalarse uno encima de otro.

Los APK anteriores pudieron quedar firmados con una clave temporal distinta. Si Android muestra **“La aplicación no se instaló porque el paquete entra en conflicto”**:

1. Abre Checkpoint anterior.
2. Exporta un respaldo JSON.
3. Desinstala la versión anterior.
4. Instala el APK 0.3.0.
5. Importa el respaldo.

A partir de esta versión, las compilaciones debug del mismo proyecto conservarán la firma incluida. Esta clave es pública y **no debe utilizarse para una publicación de producción en Google Play**.

## Compilar en GitHub Actions

1. Sube el contenido de esta carpeta a la raíz de tu repositorio.
2. Abre **Actions → Build Checkpoint APK**.
3. Pulsa **Run workflow**, o realiza un commit en `main`.
4. Espera a que la ejecución termine en verde.
5. Abre **Summary → Artifacts**.
6. Descarga `checkpoint-android-v0.3.1-debug`.
7. Extrae `app-debug.apk` del ZIP.

El workflow ejecuta pruebas unitarias antes de compilar.

## Compilar localmente

Requisitos:

- Android Studio reciente.
- JDK 17.
- Android SDK Platform 35.
- Android SDK Build Tools 35.x.

### Windows

```bat
scripts\build-apk.bat
```

### Linux o macOS

```bash
./scripts/build-apk.sh
```

El APK se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Respaldos

El respaldo JSON 0.3.0 incluye:

- Juegos y precios.
- Historial.
- Eventos del feed.
- Objetivos y reglas de alerta.
- Claves de comparación y ediciones.
- Biblioteca, compras, formato, calificaciones y notas.
- Suscripciones por juego y suscripciones activas.
- Presupuesto mensual.
- Intervalo, notificaciones y tema visual.

El importador conserva compatibilidad con respaldos anteriores de Checkpoint. No existe sincronización automática con Chrome ni con servicios en la nube.

## Limitaciones conocidas

- **Xbox sigue en beta:** sus páginas públicas cambian con frecuencia y pueden variar por producto o región.
- El historial global anterior a la fecha de seguimiento no se importa desde SteamDB; el gráfico representa los datos reunidos por Checkpoint.
- El pronóstico de oferta necesita varios cambios reales de precio para producir una estimación útil.
- La fecha de finalización solo aparece cuando la tienda la expone de forma interpretable.
- La disponibilidad en suscripciones puede cambiar y no existe una fuente pública universal suficientemente estable; por eso se puede corregir manualmente.
- Una wishlist de Steam debe ser pública y exponer identificadores que el importador pueda leer.
- El presupuesto trabaja con la moneda predominante entre tus juegos para no sumar pesos, dólares y yenes como si fueran estampitas.
- Android puede retrasar WorkManager para ahorrar batería. “Cada hora” es una ventana aproximada, no una promesa grabada en piedra.

## Estructura técnica

- Kotlin.
- Jetpack Compose.
- Room con migración 1 → 2.
- WorkManager.
- App Widget clásico con `RemoteViews`.
- Parsers por tienda.
- Arquitectura local sin backend.

## Paquete

```text
com.entrecheckpoints.checkpoint
```

Versión: `0.3.1`  
Código de versión: `6`

Consulta [CHANGELOG.md](CHANGELOG.md) para el registro detallado de cada versión.
