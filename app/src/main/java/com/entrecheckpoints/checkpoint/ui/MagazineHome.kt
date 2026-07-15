package com.entrecheckpoints.checkpoint.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.entrecheckpoints.checkpoint.data.analytics.DealAnalytics
import com.entrecheckpoints.checkpoint.data.local.GameEntity
import com.entrecheckpoints.checkpoint.data.local.GameEventEntity
import com.entrecheckpoints.checkpoint.data.local.PricePointEntity
import com.entrecheckpoints.checkpoint.data.model.AlertEventType
import com.entrecheckpoints.checkpoint.data.model.Store
import com.entrecheckpoints.checkpoint.data.model.SubscriptionService
import com.entrecheckpoints.checkpoint.ui.components.Barcode
import com.entrecheckpoints.checkpoint.ui.components.CheckpointPanel
import com.entrecheckpoints.checkpoint.ui.components.TechnicalLabel
import com.entrecheckpoints.checkpoint.ui.components.storeColor
import com.entrecheckpoints.checkpoint.ui.theme.CheckpointStyle
import com.entrecheckpoints.checkpoint.ui.theme.CheckpointThemeMode
import com.entrecheckpoints.checkpoint.ui.theme.checkpointShape
import java.util.Locale
import kotlin.math.max

/**
 * Portada editorial dinámica de Checkpoint 1.0.
 *
 * La composición se alimenta exclusivamente de datos locales: productos seguidos,
 * historial, eventos, objetivos y suscripciones. No descarga una portada prefabricada.
 */
@Composable
fun MagazineHome(
    games: List<GameEntity>,
    historyByGame: Map<String, List<PricePointEntity>>,
    events: List<GameEventEntity>,
    activeSubscriptions: Set<SubscriptionService>,
    busy: Boolean,
    onOpen: (String) -> Unit,
    onAdd: () -> Unit,
    onRefreshAll: () -> Unit,
    onOpenDeals: () -> Unit,
) {
    val feature = remember(games, historyByGame, events) {
        MagazineEditorial.selectFeature(games, historyByGame, events)
    }
    val issue = remember { MagazineEditorial.issueData() }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        MagazineCover(
            feature = feature,
            games = games,
            events = events,
            activeSubscriptions = activeSubscriptions,
            issue = issue,
            busy = busy,
            onOpen = onOpen,
            onAdd = onAdd,
            onRefreshAll = onRefreshAll,
        )
        if (games.isNotEmpty()) {
            MagazineDealsRail(
                games = games,
                historyByGame = historyByGame,
                onOpen = onOpen,
                onOpenDeals = onOpenDeals,
            )
            PricePulsePanel(games, historyByGame)
        }
    }
}

@Composable
private fun MagazineCover(
    feature: MagazineFeature?,
    games: List<GameEntity>,
    events: List<GameEventEntity>,
    activeSubscriptions: Set<SubscriptionService>,
    issue: MagazineIssue,
    busy: Boolean,
    onOpen: (String) -> Unit,
    onAdd: () -> Unit,
    onRefreshAll: () -> Unit,
) {
    val style = CheckpointStyle.current
    val featuredGame = feature?.game
    val currency = featuredGame?.currency ?: games.firstOrNull()?.currency ?: "MXN"
    val comparableGames = games.filter { it.currency == currency }
    val totalSavings = comparableGames.sumOf { (it.regularPriceCents - it.priceCents).coerceAtLeast(0L) }
    val unseenOffers = events.count {
        !it.seen && AlertEventType.fromId(it.type) != AlertEventType.SOURCE_ERROR
    }
    val targets = games.count { it.targetPriceCents != null && it.priceCents <= it.targetPriceCents }
    val included = games.count { game ->
        SubscriptionService.fromCsv(game.subscriptionTags).any { service -> service in activeSubscriptions }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(style.surfaceRaised, checkpointShape())
            .border(1.dp, style.borderStrong, checkpointShape())
            .clip(checkpointShape()),
    ) {
        MagazineMasthead(
            issue = issue,
            tracked = games.size,
            busy = busy,
            onAdd = onAdd,
            onRefreshAll = onRefreshAll,
        )
        HorizontalDivider(color = style.borderStrong)
        if (feature == null) {
            EmptyMagazineCover(onAdd)
        } else {
            MagazineHero(feature = feature, onOpen = { onOpen(feature.game.id) })
        }
        HorizontalDivider(color = style.borderStrong)
        Row(
            Modifier
                .fillMaxWidth()
                .background(style.surfaceRaised)
                .padding(horizontal = 6.dp, vertical = 7.dp),
        ) {
            CoverMetric(
                value = unseenOffers.toString().padStart(2, '0'),
                label = "NUEVAS\nOFERTAS",
                modifier = Modifier.weight(1f),
            )
            CoverMetric(
                value = targets.toString().padStart(2, '0'),
                label = "OBJETIVOS\nALCANZADOS",
                modifier = Modifier.weight(1f),
            )
            CoverMetric(
                value = formatCompactMoney(totalSavings, currency),
                label = "AHORRO\nDISPONIBLE",
                modifier = Modifier.weight(1.25f),
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .background(style.surfaceAlt)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TechnicalLabel("Steam / Xbox / Nintendo", color = style.ink)
            TechnicalLabel(
                if (included > 0) "$included incluidos en tus servicios" else "datos locales · sin cuenta",
                color = style.muted,
            )
        }
    }
}

@Composable
private fun MagazineMasthead(
    issue: MagazineIssue,
    tracked: Int,
    busy: Boolean,
    onAdd: () -> Unit,
    onRefreshAll: () -> Unit,
) {
    val style = CheckpointStyle.current
    Column(
        Modifier
            .fillMaxWidth()
            .background(style.surfaceRaised)
            .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TechnicalLabel("Track. Compare. Save.", color = style.muted, modifier = Modifier.weight(1f))
            TechnicalLabel("Tu revista de ofertas gamer", color = style.muted)
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text(
                    "CHECKPOINT",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    letterSpacing = (-1.8).sp,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("by ", style = MaterialTheme.typography.titleMedium, color = style.ink)
                    Text(
                        "ENTRE CHECKPOINTS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = style.accentSecondary,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                TechnicalLabel("ISSUE ${issue.number}", color = style.ink)
                TechnicalLabel(issue.dateLabel, color = style.muted)
                Barcode(issue.barcodeSeed, Modifier.width(80.dp).height(18.dp))
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TechnicalLabel("$tracked productos bajo vigilancia", color = style.muted, modifier = Modifier.weight(1f))
            IconButton(onClick = onRefreshAll, enabled = !busy, modifier = Modifier.size(32.dp)) {
                if (busy) {
                    CircularProgressIndicator(Modifier.size(17.dp), strokeWidth = 2.dp, color = style.accentSecondary)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar precios", tint = style.ink, modifier = Modifier.size(19.dp))
                }
            }
            IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Agregar juego", tint = style.ink, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun MagazineHero(feature: MagazineFeature, onOpen: () -> Unit) {
    val style = CheckpointStyle.current
    val game = feature.game
    val store = Store.fromId(game.storeId)
    val storeAccent = storeColor(store)
    val coverAccent = brightenForDarkBackground(storeAccent)
    val matrix = remember(style.coverSaturation) { ColorMatrix().apply { setToSaturation(style.coverSaturation) } }

    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(0.74f)
            .background(
                Brush.linearGradient(
                    listOf(style.surfaceAlt, style.accentSoft, style.background),
                ),
            )
            .clickable(onClick = onOpen),
    ) {
        if (!game.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = game.imageUrl,
                contentDescription = game.title,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(matrix),
            )
        }
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.05f),
                            Color.Black.copy(alpha = 0.20f),
                            Color.Black.copy(alpha = 0.82f),
                            Color.Black.copy(alpha = 0.96f),
                        ),
                    ),
                ),
        )
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Black.copy(alpha = 0.42f), Color.Transparent, Color.Black.copy(alpha = 0.08f)),
                    ),
                ),
        )

        Column(
            Modifier.fillMaxSize().padding(15.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier
                        .background(storeAccent)
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("★", color = readableTextOn(storeAccent), fontWeight = FontWeight.Black, fontSize = 10.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(feature.kicker, color = readableTextOn(storeAccent), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
                }
                Row(
                    Modifier
                        .background(Color.Black.copy(alpha = 0.66f))
                        .border(1.dp, Color.White.copy(alpha = 0.35f))
                        .padding(horizontal = 7.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(7.dp).background(storeAccent))
                    Spacer(Modifier.width(5.dp))
                    Text(store.displayName.uppercase(), color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    feature.headline,
                    color = Color.White,
                    fontSize = 42.sp,
                    lineHeight = 38.sp,
                    letterSpacing = (-1.3).sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 3,
                )
                Text(
                    game.title.uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f)) {
                        TechnicalLabel("Precio actual", color = coverAccent)
                        Text(
                            formatPrice(game.priceCents, game.currency),
                            color = Color.White,
                            fontSize = 39.sp,
                            lineHeight = 40.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1.2).sp,
                            maxLines = 1,
                        )
                        if (game.regularPriceCents > game.priceCents) {
                            Text(
                                "ANTES ${formatPrice(game.regularPriceCents, game.currency)}",
                                color = Color.White.copy(alpha = 0.70f),
                                style = MaterialTheme.typography.labelLarge,
                                textDecoration = TextDecoration.LineThrough,
                            )
                        }
                    }
                    if (game.discountPercent > 0) {
                        Column(
                            Modifier
                                .background(storeAccent)
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                "-${game.discountPercent}%",
                                color = readableTextOn(storeAccent),
                                fontSize = 29.sp,
                                lineHeight = 29.sp,
                                fontWeight = FontWeight.Black,
                            )
                            Text("OFF", color = readableTextOn(storeAccent), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TechnicalLabel(feature.detail, color = Color.White.copy(alpha = 0.78f), modifier = Modifier.weight(1f))
                    TechnicalLabel("Abrir ficha →", color = coverAccent)
                }
            }
        }
    }
}

@Composable
private fun EmptyMagazineCover(onAdd: () -> Unit) {
    val style = CheckpointStyle.current
    Column(
        Modifier
            .fillMaxWidth()
            .aspectRatio(0.84f)
            .background(
                Brush.verticalGradient(
                    listOf(style.surfaceAlt, style.background, style.accentSoft),
                ),
            )
            .padding(22.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        TechnicalLabel("Vol. 01 / Primer número", color = style.accentSecondary)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "THE FIRST\nISSUE STARTS\nHERE",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                color = style.ink,
            )
            Text(
                "Agrega un juego de Nintendo, Steam o Xbox y Checkpoint construirá esta portada con tus precios reales.",
                color = style.muted,
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(onClick = onAdd, shape = checkpointShape()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("CREAR MI PRIMERA PORTADA")
            }
        }
        Barcode("CHECKPOINT-FIRST-ISSUE", Modifier.fillMaxWidth().height(28.dp))
    }
}

@Composable
private fun CoverMetric(value: String, label: String, modifier: Modifier = Modifier) {
    val style = CheckpointStyle.current
    Row(modifier.height(64.dp)) {
        Column(
            Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                value,
                color = style.accentSecondary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                label,
                color = style.ink,
                style = MaterialTheme.typography.labelMedium,
                lineHeight = 11.sp,
                maxLines = 2,
            )
        }
        Box(Modifier.width(1.dp).fillMaxHeight().background(style.hairline))
    }
}

@Composable
private fun MagazineDealsRail(
    games: List<GameEntity>,
    historyByGame: Map<String, List<PricePointEntity>>,
    onOpen: (String) -> Unit,
    onOpenDeals: () -> Unit,
) {
    val style = CheckpointStyle.current
    val topDeals = remember(games, historyByGame) {
        val discounted = games.filter { it.discountPercent > 0 }
        (discounted.ifEmpty { games })
            .sortedByDescending { DealAnalytics.calculate(it, historyByGame[it.id].orEmpty()).dealScore }
            .take(6)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("BEST DEALS TODAY", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                TechnicalLabel("Selección editorial según Deal Score", color = style.muted)
            }
            TextButton(onClick = onOpenDeals) {
                Text("VER TODO")
                Spacer(Modifier.width(3.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
        LazyRow(
            contentPadding = PaddingValues(end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            items(topDeals, key = { "cover-deal-${it.id}" }) { game ->
                MagazineDealTile(game, historyByGame[game.id].orEmpty()) { onOpen(game.id) }
            }
        }
    }
}

@Composable
private fun MagazineDealTile(
    game: GameEntity,
    history: List<PricePointEntity>,
    onOpen: () -> Unit,
) {
    val style = CheckpointStyle.current
    val analytics = remember(game, history) { DealAnalytics.calculate(game, history) }
    val storeAccent = storeColor(Store.fromId(game.storeId))
    val matrix = remember(style.coverSaturation) { ColorMatrix().apply { setToSaturation(style.coverSaturation) } }
    Column(
        Modifier
            .width(154.dp)
            .background(style.surfaceRaised, checkpointShape())
            .border(1.dp, style.hairline, checkpointShape())
            .clip(checkpointShape())
            .clickable(onClick = onOpen),
    ) {
        Box(Modifier.fillMaxWidth().height(92.dp).background(style.surfaceAlt)) {
            if (!game.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = game.imageUrl,
                    contentDescription = game.title,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.colorMatrix(matrix),
                )
            }
            Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)))))
            if (game.discountPercent > 0) {
                Text(
                    "-${game.discountPercent}%",
                    modifier = Modifier.align(Alignment.BottomStart).background(storeAccent).padding(horizontal = 6.dp, vertical = 3.dp),
                    color = readableTextOn(storeAccent),
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(game.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(formatPrice(game.priceCents, game.currency), fontWeight = FontWeight.Black, color = style.ink)
            TechnicalLabel("Score ${String.format(Locale.US, "%.1f", analytics.dealScore)}", color = style.muted)
        }
    }
}

@Composable
private fun PricePulsePanel(
    games: List<GameEntity>,
    historyByGame: Map<String, List<PricePointEntity>>,
) {
    val style = CheckpointStyle.current
    val movement = remember(games, historyByGame) { MagazineEditorial.movement(games, historyByGame) }
    val points = remember(games, historyByGame) { MagazineEditorial.pulsePoints(games, historyByGame) }
    CheckpointPanel(Modifier.fillMaxWidth(), padding = 13.dp) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("PRICE PULSE", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                TechnicalLabel(
                    "${movement.down} bajaron · ${movement.stable} sin cambios · ${movement.up} subieron",
                    color = style.muted,
                )
            }
            TechnicalLabel("${movement.errors} errores", color = if (movement.errors > 0) style.warning else style.success)
        }
        Spacer(Modifier.height(10.dp))
        Canvas(Modifier.fillMaxWidth().height(66.dp)) {
            if (points.size < 2) {
                drawLine(style.hairline, start = androidx.compose.ui.geometry.Offset(0f, size.height / 2), end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2), strokeWidth = 1.dp.toPx())
                return@Canvas
            }
            val minValue = points.minOrNull() ?: 0f
            val maxValue = points.maxOrNull() ?: 1f
            val range = max(maxValue - minValue, 0.001f)
            val step = size.width / (points.size - 1)
            val path = Path()
            points.forEachIndexed { index, value ->
                val x = index * step
                val y = size.height - ((value - minValue) / range) * size.height
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawLine(style.hairline, start = androidx.compose.ui.geometry.Offset(0f, size.height / 2), end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2), strokeWidth = 1.dp.toPx())
            if (style.mode == CheckpointThemeMode.ARCADE_NEON) {
                drawPath(path, style.accent.copy(alpha = 0.18f), style = Stroke(width = 8.dp.toPx()))
            }
            drawPath(path, style.accentSecondary, style = Stroke(width = 3.dp.toPx()))
        }
    }
}

private fun readableTextOn(color: Color): Color {
    val luminance = 0.2126f * color.red + 0.7152f * color.green + 0.0722f * color.blue
    return if (luminance > 0.56f) Color.Black else Color.White
}

private fun brightenForDarkBackground(color: Color, amount: Float = 0.38f): Color = Color(
    red = color.red + (1f - color.red) * amount,
    green = color.green + (1f - color.green) * amount,
    blue = color.blue + (1f - color.blue) * amount,
    alpha = 1f,
)

private fun formatCompactMoney(cents: Long, currency: String): String {
    if (cents <= 0L) return formatPrice(0, currency)
    val amount = cents / 100.0
    return when {
        amount >= 1_000_000 -> String.format(Locale.US, "%.1fM %s", amount / 1_000_000.0, currency)
        amount >= 10_000 -> String.format(Locale.US, "%.1fK %s", amount / 1_000.0, currency)
        else -> formatPrice(cents, currency)
    }
}
