package com.example.myapplication.pages.profile.components

import android.graphics.Paint
import android.graphics.PointF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

@Composable
fun BezierCurve(
    modifier: Modifier = Modifier,
    xValuesInt: List<Int>,
    yValues: List<Int>,
    points: List<Float>,
    points2: List<Float>,
    interval: Int,
    line1Color: Color = MaterialTheme.colorScheme.primary,
    line2Color: Color = MaterialTheme.colorScheme.secondary
) {
    val animationProgress = remember { Animatable(0f) }
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    LaunchedEffect(points, points2) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000)
        )
    }

    Canvas(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .fillMaxSize()
    ) {
        val textPaint = Paint().apply {
            textSize = 24f
            color = textColor.toArgb()
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val maxX = xValuesInt.maxOrNull()?.toFloat() ?: 1f
        val xSpacing = size.width / maxX

        val maxY = yValues.maxOrNull()?.toFloat() ?: 100f
        val ySpacing = if (maxY > 0) size.height / maxY else 0f

        // Draw X Axis Labels
        for (index in 0..maxX.toInt() step interval) {
            val label = if (index == 0) "" else (index / 10).toString()
            if (label.isNotEmpty()) {
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    xSpacing * index,
                    size.height + 35f,
                    textPaint
                )
            }
        }

        // Draw Y Axis Labels
        val yTextPaint = Paint().apply {
            textSize = 24f
            color = textColor.toArgb()
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        for (index in 0..maxY.toInt() step interval) {
            drawContext.canvas.nativeCanvas.drawText(
                index.toString(),
                -15f,
                size.height - (ySpacing * index) + 8f,
                yTextPaint
            )
        }

        val pointsList = listOf(points, points2)
        val colors = listOf(line1Color, line2Color)

        pointsList.forEachIndexed { listIndex, pts ->
            if (pts.isEmpty()) return@forEachIndexed
            
            val coordinates = mutableListOf<PointF>()

            pts.forEachIndexed { index, value ->
                if (index >= xValuesInt.size) return@forEachIndexed
                val x = xSpacing * xValuesInt[index]
                val y = size.height - (ySpacing * (value * animationProgress.value))

                coordinates.add(PointF(x, y))
                drawCircle(
                    colors[listIndex].copy(alpha = 0.6f),
                    radius = 5f,
                    center = Offset(x, y)
                )
            }

            if (coordinates.size < 2) return@forEachIndexed

            val path = Path().apply {
                moveTo(coordinates.first().x, coordinates.first().y)
                for (i in 1 until coordinates.size) {
                    val prev = coordinates[i - 1]
                    val curr = coordinates[i]
                    cubicTo(
                        prev.x + (curr.x - prev.x) / 2, prev.y,
                        prev.x + (curr.x - prev.x) / 2, curr.y,
                        curr.x, curr.y
                    )
                }
            }

            drawPath(
                path = path,
                color = colors[listIndex],
                style = Stroke(width = 5f)
            )
        }
    }
}
