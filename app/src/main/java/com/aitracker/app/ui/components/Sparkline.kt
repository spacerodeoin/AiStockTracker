package com.aitracker.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

/** A lightweight line chart used both as an inline sparkline and as a larger detail chart. */
@Composable
fun Sparkline(
    values: List<Double>,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 4f,
) {
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas

        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0 } ?: 1.0
        val stepX = size.width / (values.size - 1)

        val path = Path()
        values.forEachIndexed { index, value ->
            val x = stepX * index
            val normalized = (value - min) / range
            val y = size.height - (normalized.toFloat() * size.height)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth),
        )

        // Marker dot at the latest point.
        val lastX = stepX * (values.size - 1)
        val lastNorm = (values.last() - min) / range
        val lastY = size.height - (lastNorm.toFloat() * size.height)
        drawCircle(color = color, radius = strokeWidth * 1.4f, center = Offset(lastX, lastY))
    }
}
