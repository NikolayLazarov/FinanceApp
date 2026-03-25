package com.example.myapplication.pages.statistics

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.pages.main.common.components.Title
import com.example.myapplication.pages.statistics.components.BarChart
import java.util.Calendar
import java.util.Locale

@Composable
fun GraphsPage(modifier: Modifier = Modifier) {
    val daysList = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    
    // Determine current day index (0 for Mon, 6 for Sun)
    val calendar = Calendar.getInstance()
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val currentDayIndex = when (dayOfWeek) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> -1
    }

    val categories = listOf("All", "Food", "Drinks", "Leisure", "Travel", "Health")
    var selectedCategory by remember { mutableStateOf("All") }

    // Mock data for different categories
    val categoryData = remember(selectedCategory) {
        when (selectedCategory) {
            "Food" -> listOf(10f, 25f, 15f, 30f, 45f, 60f, 20f)
            "Drinks" -> listOf(5f, 8f, 12f, 7f, 18f, 25f, 10f)
            "Leisure" -> listOf(0f, 0f, 10f, 5f, 60f, 100f, 40f)
            "Travel" -> listOf(0f, 120f, 0f, 0f, 0f, 40f, 150f)
            "Health" -> listOf(0f, 0f, 0f, 55f, 0f, 0f, 0f)
            else -> listOf(40f, 90f, 70f, 110f, 130f, 180f, 160f) // All
        }
    }

    val totalAmount = categoryData.sum()
    val maxVal = categoryData.maxOrNull() ?: 100f
    // Dynamic interval calculation for the Y-axis
    val interval = if (maxVal > 200) 50 else if (maxVal > 100) 25 else 10
    val yValues = (0..(maxVal.toInt() + interval) step interval).toList()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Title("Statistics")

        // Summary Card with total for selected category
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Weekly $selectedCategory",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    AnimatedContent(targetState = totalAmount, label = "totalAmount") { amount ->
                        Text(
                            text = String.format(Locale.US, "$%.2f", amount),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        }

        // Horizontal list of Category Filters
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) },
                    shape = MaterialTheme.shapes.medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Enhanced Bar Chart with highlighting and animation
        BarChart(
            xValues = daysList,
            yValues = yValues,
            points = categoryData,
            interval = interval,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            highlightIndex = currentDayIndex
        )

        // Legend/Info for the current day highlight
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(MaterialTheme.colorScheme.tertiary, MaterialTheme.shapes.extraSmall)
            )
            Text(
                text = "Today's spending (Highlighted)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
