package com.example.myapplication.ui.features.profile.components

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
import androidx.compose.ui.graphics.PathEffect
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
    line2Color: Color = MaterialTheme.colorScheme.secondary,
    labels: List<String> = emptyList()
) {
    val animationProgress = remember { Animatable(0f) }
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

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

        xValuesInt.forEachIndexed { index, xVal ->
            val x = xSpacing * xVal
            
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            if (index < labels.size) {
                drawContext.canvas.nativeCanvas.drawText(
                    labels[index],
                    x,
                    size.height + 35f,
                    textPaint
                )
            }
        }

        val yTextPaint = Paint().apply {
            textSize = 24f
            color = textColor.toArgb()
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        for (index in 0..maxY.toInt() step interval) {
            val y = size.height - (ySpacing * index)
            
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )

            drawContext.canvas.nativeCanvas.drawText(
                index.toString(),
                -15f,
                y + 8f,
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
                    color = Color.White,
                    radius = 6f,
                    center = Offset(x, y)
                )
                drawCircle(
                    color = colors[listIndex],
                    radius = 4f,
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
                style = Stroke(width = 6f)
            )
        }
    }
}
