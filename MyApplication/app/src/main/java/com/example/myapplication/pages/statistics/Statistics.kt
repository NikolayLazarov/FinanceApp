package com.example.myapplication.pages.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.pages.main.common.components.Title
import com.example.myapplication.pages.statistics.components.BarChart
import com.example.myapplication.pages.profile.components.BezierCurve

@Composable
fun GraphsPage(modifier: Modifier = Modifier){
    val dailyExpensesList = listOf(0f, 70f, 60f, 60f, 50f, 10f,16f,110f)
    val daysList = listOf("","Mon", "Thu", "Wed", "Thur", "Fri", "Sat", "Sun")
    val daysListInt = listOf(0,1,2,3,4,5,6,7)
    val max = dailyExpensesList.max()
    val count = 5
    val step = max/ (count - 1)  // integer division

    val tags = listOf("Food", "Drinks", "Leisure,", "Friends", "Presents")


    val yValues = List(count) { it *step.toInt() }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Title("Graphs")
            BarChart(
                xValuesInt = daysListInt,
                xValues = daysList,
                yValues = yValues,
                interval = 10,
                modifier = Modifier.size(300.dp),
                points = dailyExpensesList
            )
            BezierCurve(
                modifier = Modifier.size(300.dp),
                xValuesInt = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,13,14,15,16,17,18,19,20,21).map { it.times(10) },
                yValues = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).map { it.times(10) },
                interval = 10,
                points = listOf(
                    0f,
                    5.4f,
                    2f,
                    6f,
                    9f,
                    4f,
                    2f,
                    4f,
                    8f,
                    1f,
                    11f,
                    3f,
                    5.4f,
                    2f,
                    6f,
                    9f,
                    4f,
                    2f,
                    4f
                ).map { it.times(10f) },

                points2 = listOf(
                    0f,
                    6.4f,
                    1f,
                    5f,
                    10f,
                    5f,
                    1f,
                    2f,
                    10f,
                    7f,
                    8f,
                    5f,
                    5f,
                    2.3f,
                    6.9f,
                    7f,
                    5f,
                    6f,
                    8f
                ).map { it.times(10f) },
            )
        }
    }
}