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
    highlightIndex: Int? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val highlightColor = MaterialTheme.colorScheme.tertiary
    val containerColor = MaterialTheme.colorScheme.primaryContainer
    val onContainerColor = MaterialTheme.colorScheme.onPrimaryContainer

    // Animation state for the bars
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 1000))
    }

    Box(
        modifier = modifier
            .background(containerColor, shape = MaterialTheme.shapes.medium)
            .padding(top = 16.dp, bottom = 32.dp, start = 40.dp, end = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        val textPaint = Paint().apply {
            textSize = 30f
            color = onContainerColor.toArgb()
            textAlign = Paint.Align.CENTER
        }

        Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val maxX = xValues.size - 1
            val xSpacing = if (maxX > 0) canvasWidth.div(maxX) else canvasWidth

            val maxY = yValues.maxOrNull()?.toFloat() ?: 100f
            val ySpacing = if (maxY > 0) canvasHeight.div(maxY) else canvasHeight

            // Draw X Axis Labels
            xValues.forEachIndexed { index, day ->
                drawContext.canvas.nativeCanvas.drawText(
                    day,
                    xSpacing * index,
                    canvasHeight + 40f,
                    textPaint
                )
            }

            // Draw Y Axis Labels
            val yTextPaint = Paint().apply {
                textSize = 30f
                color = onContainerColor.toArgb()
                textAlign = Paint.Align.RIGHT
            }
            for (value in 0..maxY.toInt() step interval) {
                drawContext.canvas.nativeCanvas.drawText(
                    value.toString(),
                    -15f,
                    canvasHeight - (ySpacing * value) + 10f,
                    yTextPaint
                )
            }

            // Draw Bars
            points.forEachIndexed { index, value ->
                val x = xSpacing * index
                val animatedValue = value * animationProgress.value
                val y = canvasHeight - (ySpacing * animatedValue)

                val color = if (index == highlightIndex) highlightColor else primaryColor

                drawLine(
                    color = color,
                    start = Offset(x, canvasHeight),
                    end = Offset(x, y),
                    strokeWidth = 30f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
