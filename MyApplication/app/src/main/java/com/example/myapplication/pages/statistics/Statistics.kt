package com.example.myapplication.pages.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.pages.statistics.components.BarChart
import com.example.myapplication.pages.profile.components.BezierCurve

@Composable
fun GraphsPage(modifier: Modifier = Modifier) {
    val dailyExpensesList = listOf(0f, 70f, 60f, 60f, 50f, 10f, 16f, 110f)
    val daysList = listOf("", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val daysListInt = listOf(0, 1, 2, 3, 4, 5, 6, 7)
    val max = dailyExpensesList.max()
    val count = 5
    val step = max / (count - 1)
    val yValues = List(count) { it * step.toInt() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Your spending overview",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Weekly bar chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Weekly Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                BarChart(
                    xValues = daysList,
                    yValues = yValues,
                    interval = 10,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    points = dailyExpensesList
                )
            }
        }

        // Trend curves
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Spending Trends",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                BezierCurve(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    xValuesInt = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21).map { it.times(10) },
                    yValues = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).map { it.times(10) },
                    interval = 10,
                    points = listOf(
                        0f, 5.4f, 2f, 6f, 9f, 4f, 2f, 4f, 8f, 1f, 11f, 3f,
                        5.4f, 2f, 6f, 9f, 4f, 2f, 4f
                    ).map { it.times(10f) },
                    points2 = listOf(
                        0f, 6.4f, 1f, 5f, 10f, 5f, 1f, 2f, 10f, 7f, 8f, 5f,
                        5f, 2.3f, 6.9f, 7f, 5f, 6f, 8f
                    ).map { it.times(10f) },
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
