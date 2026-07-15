package com.entrecheckpoints.checkpoint.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrecheckpoints.checkpoint.data.local.PricePointEntity
import com.entrecheckpoints.checkpoint.data.model.Store
import com.entrecheckpoints.checkpoint.ui.theme.CheckpointPattern
import com.entrecheckpoints.checkpoint.ui.theme.CheckpointStyle
import com.entrecheckpoints.checkpoint.ui.theme.CheckpointThemeMode
import com.entrecheckpoints.checkpoint.ui.theme.checkpointShape
import kotlin.math.max

@Composable
fun Modifier.editorialTexture(): Modifier {
    val style = CheckpointStyle.current
    return this
        .background(style.background)
        .drawBehind {
            when (style.pattern) {
                CheckpointPattern.PAPER_GRID -> {
                    // Una retícula de revista muy tenue. Antes parecía hoja de cálculo con ansiedad.
                    val horizontalStep = 44.dp.toPx()
                    var y = horizontalStep
                    while (y < size.height) {
                        drawLine(
                            style.ink.copy(alpha = 0.035f),
                            Offset(0f, y),
                            Offset(size.width, y),
                            0.5.dp.toPx(),
                        )
                        y += horizontalStep
                    }
                    val verticalStep = 88.dp.toPx()
                    var x = verticalStep
                    while (x < size.width) {
                        drawLine(
                            style.accentSecondary.copy(alpha = 0.025f),
                            Offset(x, 0f),
                            Offset(x, size.height),
                            0.5.dp.toPx(),
                        )
                        x += verticalStep
                    }
                }

                CheckpointPattern.NIGHT_LINES -> {
                    val step = 36.dp.toPx()
                    var y = 0f
                    while (y < size.height) {
                        drawLine(style.hairline.copy(alpha = 0.17f), Offset(0f, y), Offset(size.width, y), 0.5.dp.toPx())
                        y += step
                    }
                    var x = -size.height
                    while (x < size.width) {
                        drawLine(
                            style.accent.copy(alpha = 0.018f),
                            Offset(x, size.height),
                            Offset(x + size.height, 0f),
                            0.8.dp.toPx(),
                        )
                        x += 92.dp.toPx()
                    }
                }

                CheckpointPattern.AERO_BUBBLES -> {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                style.background,
                                style.background.copy(alpha = 0.93f),
                                style.accentSecondary.copy(alpha = 0.34f),
                            ),
                        ),
                    )
                    val bubble = Color.White.copy(alpha = 0.16f)
                    drawCircle(bubble, radius = size.minDimension * 0.13f, center = Offset(size.width * 0.84f, size.height * 0.10f))
                    drawCircle(bubble, radius = size.minDimension * 0.075f, center = Offset(size.width * 0.16f, size.height * 0.29f))
                    drawCircle(style.accent.copy(alpha = 0.07f), radius = size.minDimension * 0.17f, center = Offset(size.width * 0.92f, size.height * 0.72f))
                }

                CheckpointPattern.NEON_GRID -> {
                    drawRect(style.background)
                    val step = 48.dp.toPx()
                    var x = 0f
                    while (x < size.width) {
                        drawLine(style.accent.copy(alpha = 0.045f), Offset(x, 0f), Offset(x, size.height), 0.6.dp.toPx())
                        x += step
                    }
                    var y = 0f
                    while (y < size.height) {
                        drawLine(style.accentSecondary.copy(alpha = 0.035f), Offset(0f, y), Offset(size.width, y), 0.6.dp.toPx())
                        y += step
                    }
                    drawLine(
                        brush = Brush.horizontalGradient(listOf(Color.Transparent, style.accent.copy(alpha = 0.55f), style.accentSecondary.copy(alpha = 0.55f), Color.Transparent)),
                        start = Offset(0f, size.height * 0.16f),
                        end = Offset(size.width, size.height * 0.16f),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            }
        }
}

@Composable
fun CheckpointPanel(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    padding: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val style = CheckpointStyle.current
    Column(
        modifier = modifier
            .background(style.surfaceRaised, checkpointShape())
            .border(1.dp, if (highlighted) style.borderStrong else style.hairline, checkpointShape())
            .padding(padding),
        content = content,
    )
}

@Composable
fun TechnicalLabel(text: String, modifier: Modifier = Modifier, color: Color? = null) {
    val resolvedColor = color ?: CheckpointStyle.current.muted
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = resolvedColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun StoreBadge(store: Store, modifier: Modifier = Modifier) {
    val style = CheckpointStyle.current
    val color = storeColor(store)
    Row(
        modifier = modifier
            .background(style.surfaceAlt, checkpointShape())
            .border(1.dp, style.hairline, checkpointShape())
            .padding(horizontal = 7.dp, vertical = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Box(Modifier.width(6.dp).height(6.dp).background(color))
        Spacer(Modifier.width(6.dp))
        TechnicalLabel(store.displayName + if (store.isBeta) " · BETA" else "", color = style.ink)
    }
}

@Composable
fun storeColor(store: Store): Color {
    val style = CheckpointStyle.current
    return when (store) {
        Store.NINTENDO -> style.nintendo
        Store.STEAM -> style.steam
        Store.XBOX -> style.xbox
    }
}

@Composable
fun Barcode(seed: String, modifier: Modifier = Modifier) {
    val ink = CheckpointStyle.current.ink.copy(alpha = 0.82f)
    Canvas(modifier = modifier.height(24.dp).fillMaxWidth()) {
        val values = seed.ifBlank { "CHECKPOINT" }.mapIndexed { index, char ->
            ((char.code + index * 7) % 4 + 1).toFloat()
        }
        val totalUnits = values.sum() + values.size
        val unit = size.width / max(totalUnits, 1f)
        var x = 0f
        values.forEachIndexed { index, widthUnits ->
            if (index % 2 == 0 || widthUnits > 2) {
                drawRect(ink, topLeft = Offset(x, 0f), size = Size(unit * widthUnits, size.height))
            }
            x += unit * (widthUnits + 1)
        }
    }
}

@Composable
fun PriceChart(points: List<PricePointEntity>, modifier: Modifier = Modifier) {
    val style = CheckpointStyle.current
    val sorted = points.sortedBy { it.checkedAt }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(style.surfaceAlt, checkpointShape())
            .border(1.dp, style.hairline, checkpointShape())
            .padding(12.dp),
    ) {
        if (sorted.size < 2) {
            TechnicalLabel("El historial aparecerá después de más revisiones", modifier = Modifier.padding(6.dp))
            return@Box
        }
        Canvas(Modifier.fillMaxWidth().height(124.dp)) {
            val min = sorted.minOf { it.priceCents }.toFloat()
            val max = sorted.maxOf { it.priceCents }.toFloat()
            val range = (max - min).coerceAtLeast(1f)
            val xStep = size.width / (sorted.size - 1).coerceAtLeast(1)
            val path = Path()
            sorted.forEachIndexed { index, point ->
                val x = xStep * index
                val normalized = (point.priceCents - min) / range
                val y = size.height - normalized * size.height
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawLine(style.hairline, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
            drawLine(style.hairline, Offset(0f, 0f), Offset(0f, size.height), 1.dp.toPx())
            if (style.mode == CheckpointThemeMode.ARCADE_NEON) {
                drawPath(path, style.accent.copy(alpha = 0.18f), style = Stroke(width = 7.dp.toPx()))
            }
            drawPath(path, style.accentSecondary, style = Stroke(width = 3.dp.toPx()))
            sorted.forEachIndexed { index, point ->
                val x = xStep * index
                val y = size.height - ((point.priceCents - min) / range) * size.height
                drawCircle(style.ink, radius = 3.dp.toPx(), center = Offset(x, y))
            }
        }
    }
}

@Composable
fun SectionRule(label: String) {
    val style = CheckpointStyle.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        TechnicalLabel(label, color = style.ink)
        Text("●", fontSize = 7.sp, color = style.accent, fontWeight = FontWeight.Black)
    }
    Spacer(Modifier.height(6.dp))
    Box(Modifier.fillMaxWidth().height(1.dp).background(style.borderStrong))
}
