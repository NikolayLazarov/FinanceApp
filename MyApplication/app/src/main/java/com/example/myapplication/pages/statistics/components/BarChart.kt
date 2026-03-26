package com.example.myapplication.pages.statistics.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    xValuesInt: List<Int>,
    xValues: List<String>,
    yValues: List<Int>,
    points: List<Float>,
    interval: Int
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = modifier
            .padding(8.dp)
            .fillMaxSize(),
    ) {
        val textPaint = Paint().apply {
            textSize = 28f
            color = textColor.toArgb()
            isAntiAlias = true
        }

        val maxX = xValuesInt.max()
        val xSpacing = size.width / maxX

        val maxY = yValues.max()
        val ySpacing = size.height / maxY

        // X axis labels
        xValues.forEachIndexed { index, day ->
            drawContext.canvas.nativeCanvas.drawText(
                day,
                xSpacing * index,
                size.height + 28,
                textPaint
            )
        }

        // Y axis labels
        for (index in 0..maxY step interval) {
            drawContext.canvas.nativeCanvas.drawText(
                if (index == 0) "" else index.toString(),
                0f,
                size.height - (ySpacing * index),
                textPaint
            )
        }

        // Bars with rounded top
        val barWidth = 16f
        points.forEachIndexed { index, value ->
            val x = xSpacing * index - barWidth / 2
            val y = size.height - (ySpacing * value)
            val barHeight = size.height - y

            // Background bar (ghost)
            drawRoundRect(
                color = primaryContainerColor.copy(alpha = 0.3f),
                topLeft = Offset(x, 0f),
                size = Size(barWidth, size.height),
                cornerRadius = CornerRadius(barWidth / 2)
            )

            // Actual bar
            if (barHeight > 0) {
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2)
                )
            }
        }
    }
}
