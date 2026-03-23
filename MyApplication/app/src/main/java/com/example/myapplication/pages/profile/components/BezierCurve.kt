package com.example.myapplication.pages.profile.components

import android.graphics.Paint
import android.graphics.PathMeasure
import android.graphics.PointF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed

@Composable
fun BezierCurve(
    modifier: Modifier = Modifier,
    xValuesInt: List<Int>,
    yValues: List<Int>,
    points: List<Float>,
    points2: List<Float>,
    interval: Int
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(2000)
        )
    }

    Box(modifier.size(300.dp).background(lightColorScheme().surfaceContainer).padding(), contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize()
        ) {

            val textPaint = Paint().apply {
                textSize = 24f
                color = Color.Black.toArgb()
            }

            val maxX = xValuesInt.max()
            val xSpacing = size.width.div(maxX)

            val maxY = yValues.max()
            val ySpacing = size.height.div(maxY)

            for (index in 0..maxX step interval) {
                drawContext.canvas.nativeCanvas.drawText(
                    if (index == 0) "" else (index/10).toString(),
                    xSpacing.times(index),
                    size.height,
                    textPaint
                )
            }

            for (index in 0..maxY step interval) {
                drawContext.canvas.nativeCanvas.drawText(
                    if (index == 0) "" else index.toString(),
                    0f,
                    size.height - (ySpacing.times(index)),
                    textPaint
                )
            }
            val pointsList = listOf(points, points2);
            var current = 0;
            for (points in pointsList){
                val coordinatesCl1 = mutableListOf<PointF>()

                points.fastForEachIndexed { index, value ->
                    val x = xSpacing.times(xValuesInt.get(index))
                    val y = size.height - (ySpacing.times(value))

                    coordinatesCl1.add(PointF(x, y))
                    drawCircle(
                        lightColorScheme().secondary,
                        radius = 10f,
                        center = Offset(x, y)
                    )
                }

              val controlPoints = calculateControlPoints(coordinatesCl1)


                val path1 = Path().apply {
                    reset()
                    moveTo(
                        coordinatesCl1.first().x,
                        coordinatesCl1.first().y
                    )
                    for (point in 1 until coordinatesCl1.size) {
                        val controlPoint = controlPoints.get(point - 1)
                        cubicTo(
                            controlPoint.first.x, controlPoint.first.y,
                            controlPoint.second.x, controlPoint.second.y,
                            coordinatesCl1.get(point).x, coordinatesCl1.get(point).y
                        )
                    }
                }

                val pathMeasure = PathMeasure(path1.asAndroidPath(), false)
                val animationPath = android.graphics.Path()

                pathMeasure.getSegment(
                    0f,
                    pathMeasure.length * animationProgress.value,
                    animationPath,
                    true
                )

                drawPath(
                    Path().apply { addPath(animationPath.asComposePath()) },
                    color = if (current == 0) lightColorScheme().primary else lightColorScheme().secondary,
                    style = Stroke(width = 12f)
                )
                current +=1
            }



        }
    }

}

fun calculateControlPoints(points: MutableList<PointF>): List<Pair<PointF, PointF>> {
    val controlPoints = mutableListOf<Pair<PointF, PointF>>()
    for (i in 1 until points.size) {
        val previous = points.get(i - 1)
        val current = points.get(i)
        val next = points.getOrNull(i + 1)

        val c1 = PointF(
            previous.x + (current.x - previous.x).div(3),
            previous.y + (current.y - previous.y).div(3)
        )
        val c2 = next?.let {
            PointF(
                current.x - (next.x - previous.x).div(3f),
                current.y - (next.y - previous.y).div(3)
            )
        } ?: run {
            PointF(
                previous.x - (current.x - previous.x).div(3),
                previous.y - (current.y - previous.y).div(3)
            )
        }
        controlPoints.add(
            Pair(c1, c2)
        )
    }
    return controlPoints
}