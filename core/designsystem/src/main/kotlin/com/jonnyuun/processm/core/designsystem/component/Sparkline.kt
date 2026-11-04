package com.jonnyuun.processm.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Gráfica de línea (sparkline) para mostrar la evolución de una métrica en el
 * tiempo. Los valores deben estar normalizados entre 0f y 1f.
 */
@Composable
fun Sparkline(
    values: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MiuixTheme.colorScheme.primary,
    height: Dp = 52.dp,
) {
    if (values.size < 2) return

    val min = values.minOrNull() ?: 0f
    val max = values.maxOrNull() ?: 1f
    val range = (max - min).coerceAtLeast(0.01f)

    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        val w = size.width
        val h = size.height
        val step = w / (values.size - 1)

        // Líneas guía.
        val mid = (min + max) / 2f
        for (guide in listOf(min, mid, max)) {
            val y = h - ((guide - min) / range) * h
            drawLine(
                color = color.copy(alpha = 0.12f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f,
            )
        }

        val points = values.mapIndexed { index, value ->
            Offset(index * step, h - ((value - min) / range) * h)
        }

        // Relleno bajo la curva.
        val fill = Path().apply {
            moveTo(points.first().x, h)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, h)
            close()
        }
        drawPath(
            path = fill,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.28f), color.copy(alpha = 0.02f)),
                startY = 0f,
                endY = h,
            ),
        )

        // Línea principal.
        val line = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }
        drawPath(
            path = line,
            color = color,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        // Punto final.
        drawCircle(color = color, radius = 4f, center = points.last())
        drawCircle(color = Color.White, radius = 2f, center = points.last())
    }
}
EOF
echo done