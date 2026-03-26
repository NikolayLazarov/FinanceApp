package com.example.myapplication.pages.statistics

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.models.Product
import com.example.myapplication.pages.statistics.components.BarChart
import com.example.myapplication.pages.profile.components.BezierCurve
import com.example.myapplication.view.TimeGroup
import java.util.Locale

private val expenseCategories = listOf(
    "Food", "Transportation", "Utilities", "Healthcare", "Education",
    "Entertainment", "Shopping", "Travel", "Finance", "Other"
)

@Composable
fun GraphsPage(
    expenses: List<Product> = emptyList(),
    timeGroup: TimeGroup = TimeGroup.DAY,
    selectedCategory: String? = null,
    onTimeGroupChange: (TimeGroup) -> Unit = {},
    onCategoryChange: (String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Filter expenses based on selected category
    val filteredExpenses = if (selectedCategory != null) {
        expenses.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    } else {
        expenses
    }

    // Mock data for charts
    val dailyExpensesList = listOf(0f, 70f, 60f, 60f, 50f, 10f, 16f, 110f)
    val daysList = listOf("", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val max = dailyExpensesList.maxOrNull() ?: 100f
    val count = 5
    val step = max / (count - 1).coerceAtLeast(1)
    val yValues = List(count) { it * step.toInt() }

    val totalSpent = filteredExpenses.sumOf { it.amount }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(top = 48.dp, bottom = 32.dp, start = 20.dp, end = 20.dp)
        ) {
            Column {
                Text(
                    text = "Analytics",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Insights into your spending habits",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Total spent card (similar to MainPage)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            MaterialTheme.shapes.medium
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.TrendingDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Total Spent ${if (selectedCategory != null) "in $selectedCategory" else ""}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", totalSpent)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Filters Section
        Text(
            text = "Filters",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TimeGroup.entries.toList()) { group ->
                FilterChip(
                    selected = group == timeGroup,
                    onClick = { onTimeGroupChange(group) },
                    label = {
                        Text(
                            group.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategoryChange(null) },
                    label = {
                        Text("All Categories", style = MaterialTheme.typography.labelMedium)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                    )
                )
            }
            items(expenseCategories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        onCategoryChange(if (selectedCategory == category) null else category)
                    },
                    label = {
                        Text(category, style = MaterialTheme.typography.labelMedium)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Charts Section
        ChartCard(
            title = "Spending Distribution",
            subtitle = "By ${timeGroup.name.lowercase()}",
            icon = Icons.Outlined.BarChart
        ) {
            BarChart(
                xValues = daysList,
                yValues = yValues,
                interval = 10,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                points = dailyExpensesList
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ChartCard(
            title = "Spending Trends",
            subtitle = "Compared to previous period",
            icon = Icons.Outlined.AutoGraph
        ) {
            BezierCurve(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
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

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun ChartCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}
