package com.example.myapplication.pages.statistics.components

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    xValues: List<String>,
    yValues: List<Int>,
    points: List<Float>,
    interval: Int,
    barColors: List<Color> = emptyList()
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val onContainerColor = MaterialTheme.colorScheme.onSurfaceVariant

    // Animation state for the bars
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 1000))
    }

    Box(
        modifier = modifier
            .background(containerColor, shape = MaterialTheme.shapes.medium)
            .padding(top = 16.dp, bottom = 40.dp, start = 45.dp, end = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        val textPaint = Paint().apply {
            textSize = 24f
            color = onContainerColor.toArgb()
            textAlign = Paint.Align.CENTER
        }

        Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val numEntries = xValues.size
            val xSpacing = if (numEntries > 1) canvasWidth.div(numEntries - 1) else canvasWidth

            val maxY = yValues.maxOrNull()?.toFloat() ?: 100f
            val ySpacing = if (maxY > 0) canvasHeight.div(maxY) else canvasHeight

            // Draw X Axis Labels
            xValues.forEachIndexed { index, label ->
                if (label.isNotEmpty()) {
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        xSpacing * index,
                        canvasHeight + 40f,
                        textPaint
                    )
                }
            }

            // Draw Y Axis Labels
            val yTextPaint = Paint().apply {
                textSize = 24f
                color = onContainerColor.toArgb()
                textAlign = Paint.Align.RIGHT
            }
            for (value in 0..maxY.toInt() step interval) {
                drawContext.canvas.nativeCanvas.drawText(
                    value.toString(),
                    -15f,
                    canvasHeight - (ySpacing * value) + 8f,
                    yTextPaint
                )
            }

            // Draw Bars
            points.forEachIndexed { index, value ->
                // Skip drawing if it's the padding entry (empty label)
                if (index < xValues.size && xValues[index].isEmpty()) return@forEachIndexed

                val x = xSpacing * index
                val animatedValue = value * animationProgress.value
                val y = canvasHeight - (ySpacing * animatedValue)

                val color = if (index < barColors.size) {
                    barColors[index]
                } else {
                    primaryColor
                }

                // Don't draw zero-height bars to keep it clean
                if (animatedValue > 0) {
                    drawLine(
                        color = color,
                        start = Offset(x, canvasHeight),
                        end = Offset(x, y),
                        strokeWidth = (xSpacing * 0.4f).coerceIn(8f, 35f),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
