package com.example.myapplication.ui.features.statistics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BarChart(
    xValues: List<String>,
    yValues: List<Int>,
    interval: Int,
    points: List<Float>,
    barColors: List<Color>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val strokeWidth = with(density) { 1.dp.toPx() }
    val labelSize = with(density) { 10.sp.toPx() }

    Row(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            yValues.asReversed().forEach {
                Text(
                    text = it.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height - 40f
            val maxVal = yValues.maxOrNull()?.toFloat() ?: 1f

            val barWidth = (width / xValues.size) * 0.6f
            val spacing = (width / xValues.size) * 0.4f

            for (i in yValues.indices) {
                val y = height - (yValues[i] / maxVal) * height
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = strokeWidth
                )
            }

            points.forEachIndexed { index, value ->
                if (index == 0) return@forEachIndexed
                val x = spacing / 2 + index * (barWidth + spacing)
                val barHeight = (value / maxVal) * height
                
                drawRoundRect(
                    color = barColors.getOrElse(index) { Color.Gray },
                    topLeft = Offset(x, height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        xValues.getOrElse(index) { "" },
                        x + barWidth / 2,
                        size.height - 10f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = labelSize
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }
}
