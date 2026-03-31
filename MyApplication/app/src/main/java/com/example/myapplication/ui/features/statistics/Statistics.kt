package com.example.myapplication.ui.features.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.Product
import com.example.myapplication.data.model.TimeGroup
import com.example.myapplication.ui.features.statistics.components.BarChart
import com.example.myapplication.ui.features.profile.components.BezierCurve
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

private val expenseCategories = listOf(
    "Food", "Transportation", "Utilities", "Healthcare", "Education",
    "Entertainment", "Shopping", "Travel", "Finance", "Other"
)

private fun getCategoryColor(category: String?): Color {
    return when (category?.lowercase()) {
        "food" -> Color(0xFF673AB7)
        "transportation" -> Color(0xFF00BCD4)
        "utilities" -> Color(0xFFFFC107)
        "healthcare" -> Color(0xFFF44336)
        "education" -> Color(0xFF2196F3)
        "entertainment" -> Color(0xFF9C27B0)
        "shopping" -> Color(0xFFFF5722)
        "travel" -> Color(0xFF4CAF50)
        "finance" -> Color(0xFF607D8B)
        else -> Color(0xFF795548)
    }
}

@Composable
fun GraphsPage(
    expenses: List<Product> = emptyList(),
    timeGroup: TimeGroup = TimeGroup.DAY,
    selectedCategory: String? = null,
    onTimeGroupChange: (TimeGroup) -> Unit = {},
    onCategoryChange: (String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentPivotDate by remember { mutableStateOf(LocalDate.now()) }
    val weekFields = WeekFields.of(Locale.getDefault())

    val windowExpenses = remember(expenses, timeGroup, currentPivotDate) {
        expenses.filter { expense ->
            try {
                val date = LocalDate.parse(expense.date.substring(0, 10))
                when (timeGroup) {
                    TimeGroup.DAY -> date == currentPivotDate
                    TimeGroup.WEEK -> {
                        val start = currentPivotDate.with(weekFields.dayOfWeek(), 1)
                        val end = start.plusDays(6)
                        !date.isBefore(start) && !date.isAfter(end)
                    }
                    TimeGroup.MONTH -> date.month == currentPivotDate.month && date.year == currentPivotDate.year
                    TimeGroup.YEAR -> date.year == currentPivotDate.year
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    val filteredExpenses = remember(windowExpenses, selectedCategory) {
        if (selectedCategory != null) {
            windowExpenses.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        } else {
            windowExpenses
        }
    }

    val totalSpent = filteredExpenses.sumOf { it.amount }

    val (chartLabels, chartPoints, chartSubtitle) = remember(expenses, timeGroup, selectedCategory, windowExpenses, currentPivotDate) {
        if (selectedCategory == null) {
            val labels = listOf("") + expenseCategories.map { it.take(4) }
            val values = listOf(0f) + expenseCategories.map { cat ->
                windowExpenses.filter { it.category.equals(cat, ignoreCase = true) }
                    .sumOf { it.amount }.toFloat()
            }
            Triple(labels, values, "Breakdown by Category")
        } else {
            val (labels, values) = when (timeGroup) {
                TimeGroup.DAY -> {
                    val dates = (0..6).map { currentPivotDate.minusDays(it.toLong()) }.reversed()
                    val l = listOf("") + dates.map { it.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() } }
                    val v = listOf(0f) + dates.map { date ->
                        expenses.filter {
                            try {
                                val d = LocalDate.parse(it.date.substring(0, 10))
                                d == date && it.category.equals(selectedCategory, ignoreCase = true)
                            } catch (e: Exception) { false }
                        }.sumOf { it.amount }.toFloat()
                    }
                    l to v
                }
                TimeGroup.WEEK -> {
                    val labels = listOf("") + (0..3).map { i ->
                        val d = currentPivotDate.minusWeeks(i.toLong())
                        "W${d.get(weekFields.weekOfWeekBasedYear())}"
                    }.reversed()
                    val values = listOf(0f) + (0..3).map { i ->
                        val dateInWeek = currentPivotDate.minusWeeks(i.toLong())
                        val start = dateInWeek.with(weekFields.dayOfWeek(), 1)
                        val end = start.plusDays(6)
                        expenses.filter {
                            try {
                                val d = LocalDate.parse(it.date.substring(0, 10))
                                it.category.equals(selectedCategory, ignoreCase = true) && !d.isBefore(start) && !d.isAfter(end)
                            } catch (e: Exception) { false }
                        }.sumOf { it.amount }.toFloat()
                    }.reversed()
                    labels to values
                }
                TimeGroup.MONTH -> {
                    val labels = listOf("") + (0..5).map { i ->
                        currentPivotDate.minusMonths(i.toLong()).month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
                    }.reversed()
                    val values = listOf(0f) + (0..5).map { i ->
                        val target = currentPivotDate.minusMonths(i.toLong())
                        expenses.filter {
                            try {
                                val d = LocalDate.parse(it.date.substring(0, 10))
                                it.category.equals(selectedCategory, ignoreCase = true) && d.month == target.month && d.year == target.year
                            } catch (e: Exception) { false }
                        }.sumOf { it.amount }.toFloat()
                    }.reversed()
                    labels to values
                }
                TimeGroup.YEAR -> {
                    val labels = listOf("") + (0..4).map { i ->
                        currentPivotDate.minusYears(i.toLong()).year.toString()
                    }.reversed()
                    val values = listOf(0f) + (0..4).map { i ->
                        val targetYear = currentPivotDate.minusYears(i.toLong()).year
                        expenses.filter {
                            try {
                                val d = LocalDate.parse(it.date.substring(0, 10))
                                it.category.equals(selectedCategory, ignoreCase = true) && d.year == targetYear
                            } catch (e: Exception) { false }
                        }.sumOf { it.amount }.toFloat()
                    }.reversed()
                    labels to values
                }
            }
            Triple(labels, values, "Spending over Time")
        }
    }

    val maxChartValue = chartPoints.maxOrNull() ?: 100f
    val yIntervalCount = 5
    val yStep = (maxChartValue / (yIntervalCount - 1)).coerceAtLeast(1f)
    val chartYValues = List(yIntervalCount) { (it * yStep).toInt() }

    val chartColors = remember(selectedCategory, chartPoints) {
        if (selectedCategory == null) {
            listOf(Color.Transparent) + expenseCategories.map { getCategoryColor(it) }
        } else {
            List(chartPoints.size) { getCategoryColor(selectedCategory) }
        }
    }

    val trendData = remember(expenses, selectedCategory, timeGroup, currentPivotDate) {
        when (timeGroup) {
            TimeGroup.DAY -> {
                val dates1 = (0..6).map { currentPivotDate.minusDays(it.toLong()) }.reversed()
                val dates2 = (7..13).map { currentPivotDate.minusDays(it.toLong()) }.reversed()
                val labels = dates1.map { it.dayOfWeek.name.take(3) }
                val v1 = dates1.map { date ->
                    expenses.filter {
                        try {
                            val d = LocalDate.parse(it.date.substring(0, 10))
                            d == date && (selectedCategory == null || it.category.equals(selectedCategory, ignoreCase = true))
                        } catch (e: Exception) { false }
                    }.sumOf { it.amount }.toFloat()
                }
                val v2 = dates2.map { date ->
                    expenses.filter {
                        try {
                            val d = LocalDate.parse(it.date.substring(0, 10))
                            d == date && (selectedCategory == null || it.category.equals(selectedCategory, ignoreCase = true))
                        } catch (e: Exception) { false }
                    }.sumOf { it.amount }.toFloat()
                }
                TrendData(v1, v2, "Current 7 Days vs Previous 7 Days", labels)
            }
            TimeGroup.WEEK -> {
                val weeks1 = (0..3).map { currentPivotDate.minusWeeks(it.toLong()) }.reversed()
                val weeks2 = (4..7).map { currentPivotDate.minusWeeks(it.toLong()) }.reversed()
                val labels = weeks1.map { "W${it.get(weekFields.weekOfWeekBasedYear())}" }
                val v1 = weeks1.map { dateInWeek ->
                    val start = dateInWeek.with(weekFields.dayOfWeek(), 1)
                    val end = start.plusDays(6)
                    expenses.filter {
                        try {
                            val d = LocalDate.parse(it.date.substring(0, 10))
                            (selectedCategory == null || it.category.equals(selectedCategory, ignoreCase = true)) && !d.isBefore(start) && !d.isAfter(end)
                        } catch (e: Exception) { false }
                    }.sumOf { it.amount }.toFloat()
                }
                val v2 = weeks2.map { dateInWeek ->
                    val start = dateInWeek.with(weekFields.dayOfWeek(), 1)
                    val end = start.plusDays(6)
                    expenses.filter {
                        try {
                            val d = LocalDate.parse(it.date.substring(0, 10))
                            (selectedCategory == null || it.category.equals(selectedCategory, ignoreCase = true)) && !d.isBefore(start) && !d.isAfter(end)
                        } catch (e: Exception) { false }
                    }.sumOf { it.amount }.toFloat()
                }
                TrendData(v1, v2, "Current 4 Weeks vs Previous 4 Weeks", labels)
            }
            TimeGroup.MONTH -> {
                val months1 = (0..5).map { currentPivotDate.minusMonths(it.toLong()) }.reversed()
                val months2 = (6..11).map { currentPivotDate.minusMonths(it.toLong()) }.reversed()
                val labels = months1.map { it.month.name.take(3) }
                val v1 = months1.map { target ->
                    expenses.filter {
                        try {
                            val d = LocalDate.parse(it.date.substring(0, 10))
                            (selectedCategory == null || it.category.equals(selectedCategory, ignoreCase = true)) && d.month == target.month && d.year == target.year
                        } catch (e: Exception) { false }
                    }.sumOf { it.amount }.toFloat()
                }
                val v2 = months2.map { target ->
                    expenses.filter {
                        try {
                            val d = LocalDate.parse(it.date.substring(0, 10))
                            (selectedCategory == null || it.category.equals(selectedCategory, ignoreCase = true)) && d.month == target.month && d.year == target.year
                        } catch (e: Exception) { false }
                    }.sumOf { it.amount }.toFloat()
                }
                TrendData(v1, v2, "Current 6 Months vs Previous 6 Months", labels)
            }
            TimeGroup.YEAR -> {
                val years1 = (0..4).map { currentPivotDate.minusYears(it.toLong()) }.reversed()
                val years2 = (5..9).map { currentPivotDate.minusYears(it.toLong()) }.reversed()
                val labels = years1.map { it.year.toString().takeLast(2) }
                val v1 = years1.map { target ->
                    expenses.filter {
                        try {
                            val d = LocalDate.parse(it.date.substring(0, 10))
                            (selectedCategory == null || it.category.equals(selectedCategory, ignoreCase = true)) && d.year == target.year
                        } catch (e: Exception) { false }
                    }.sumOf { it.amount }.toFloat()
                }
                val v2 = years2.map { target ->
                    expenses.filter {
                        try {
                            val d = LocalDate.parse(it.date.substring(0, 10))
                            (selectedCategory == null || it.category.equals(selectedCategory, ignoreCase = true)) && d.year == target.year
                        } catch (e: Exception) { false }
                    }.sumOf { it.amount }.toFloat()
                }
                TrendData(v1, v2, "Current 5 Years vs Previous 5 Years", labels)
            }
        }
    }

    val trendXValues = (1..trendData.p1.size).map { it * 10 }
    val maxTrend = (trendData.p1 + trendData.p2).maxOrNull() ?: 100f
    val trendYValues = List(5) { (it * (maxTrend / 4)).toInt() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                    val timeLabel = when(timeGroup) {
                        TimeGroup.DAY -> "for this Day"
                        TimeGroup.WEEK -> "for this Week"
                        TimeGroup.MONTH -> "for this Month"
                        TimeGroup.YEAR -> "for this Year"
                    }
                    val categoryText = if (selectedCategory != null) " in $selectedCategory" else ""
                    Text(
                        text = "Total Spent $timeLabel$categoryText",
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
                    onClick = { 
                        onTimeGroupChange(group)
                        currentPivotDate = LocalDate.now()
                    },
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = {
                currentPivotDate = when (timeGroup) {
                    TimeGroup.DAY -> currentPivotDate.minusDays(1)
                    TimeGroup.WEEK -> currentPivotDate.minusWeeks(1)
                    TimeGroup.MONTH -> currentPivotDate.minusMonths(1)
                    TimeGroup.YEAR -> currentPivotDate.minusYears(1)
                }
            }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
            }

            Text(
                text = getPeriodLabel(currentPivotDate, timeGroup),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(180.dp),
                textAlign = TextAlign.Center
            )

            IconButton(onClick = {
                currentPivotDate = when (timeGroup) {
                    TimeGroup.DAY -> currentPivotDate.plusDays(1)
                    TimeGroup.WEEK -> currentPivotDate.plusWeeks(1)
                    TimeGroup.MONTH -> currentPivotDate.plusMonths(1)
                    TimeGroup.YEAR -> currentPivotDate.plusYears(1)
                }
            }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next")
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
                val categoryColor = getCategoryColor(category)
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        onCategoryChange(if (selectedCategory == category) null else category)
                    },
                    label = {
                        Text(category, style = MaterialTheme.typography.labelMedium)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = categoryColor,
                        selectedLabelColor = Color.White,
                        containerColor = categoryColor.copy(alpha = 0.1f),
                        labelColor = categoryColor
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ChartCard(
            title = "Spending Distribution",
            subtitle = chartSubtitle,
            icon = Icons.Outlined.BarChart
        ) {
            BarChart(
                xValues = chartLabels,
                yValues = chartYValues,
                interval = if (yStep > 0) yStep.toInt() else 10,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                points = chartPoints,
                barColors = chartColors
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ChartCard(
            title = "Spending Trends",
            subtitle = trendData.subtitle,
            icon = Icons.Outlined.AutoGraph
        ) {
            BezierCurve(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                xValuesInt = trendXValues,
                yValues = trendYValues,
                interval = if (trendYValues.size > 1) (trendYValues[1] - trendYValues[0]).coerceAtLeast(1) else 10,
                points = trendData.p1,
                points2 = trendData.p2,
                line1Color = if (selectedCategory != null) getCategoryColor(selectedCategory) else MaterialTheme.colorScheme.primary,
                line2Color = if (selectedCategory != null) getCategoryColor(selectedCategory).copy(alpha = 0.3f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                labels = trendData.labels
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

private data class TrendData(
    val p1: List<Float>,
    val p2: List<Float>,
    val subtitle: String,
    val labels: List<String>
)

private fun getPeriodLabel(date: LocalDate, timeGroup: TimeGroup): String {
    val weekFields = WeekFields.of(Locale.getDefault())
    return when (timeGroup) {
        TimeGroup.DAY -> date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
        TimeGroup.WEEK -> {
            val start = date.with(weekFields.dayOfWeek(), 1)
            val end = start.plusDays(6)
            "${start.format(DateTimeFormatter.ofPattern("MMM d"))} - ${end.format(DateTimeFormatter.ofPattern("MMM d"))}"
        }
        TimeGroup.MONTH -> date.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        TimeGroup.YEAR -> date.year.toString()
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
