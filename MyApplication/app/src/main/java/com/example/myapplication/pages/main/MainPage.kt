package com.example.myapplication.pages.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.models.LoginResult
import com.example.myapplication.models.Product
import com.example.myapplication.pages.main.common.components.CardContainer
import com.example.myapplication.view.TimeGroup
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

val categoryNames = listOf(
    "Food", "Transportation", "Utilities", "Healthcare", "Education",
    "Entertainment", "Shopping", "Travel", "Finance", "Other"
)

@Composable
fun MainPage(
    expenses: List<Product>,
    timeGroup: TimeGroup,
    selectedCategory: String?,
    userInfo: LoginResult?,
    onTimeGroupChange: (TimeGroup) -> Unit,
    onCategoryChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Allowance cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CardContainer(
                "Daily Allowance",
                String.format("%.2f", userInfo?.dailyAllowance ?: 0.0),
                modifier = Modifier.weight(1f)
            )
            CardContainer(
                "Savings",
                String.format("%.2f", userInfo?.savings ?: 0.0),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Time group filter chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TimeGroup.entries.toList()) { group ->
                FilterChip(
                    selected = timeGroup == group,
                    onClick = { onTimeGroupChange(group) },
                    label = { Text(group.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Category filter chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategoryChange(null) },
                    label = { Text("All") }
                )
            }
            items(categoryNames) { name ->
                FilterChip(
                    selected = selectedCategory == name,
                    onClick = { onCategoryChange(name) },
                    label = { Text(name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter by category
        val filtered = if (selectedCategory != null) {
            expenses.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        } else {
            expenses
        }

        // Group expenses
        val grouped = groupExpenses(filtered, timeGroup)

        if (grouped.isEmpty()) {
            Text(
                "No expenses yet",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                grouped.forEach { (groupLabel, expensesInGroup) ->
                    item(key = "header_$groupLabel") {
                        Text(
                            text = groupLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(expensesInGroup, key = { it.id }) { expense ->
                        ExpenseRow(expense)
                    }
                    item(key = "divider_$groupLabel") {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(expense: Product) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%.2f", expense.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = formatExpenseDate(expense.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatExpenseDate(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr.substring(0, 10))
        date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    } catch (_: Exception) {
        dateStr
    }
}

private fun groupExpenses(
    expenses: List<Product>,
    timeGroup: TimeGroup
): Map<String, List<Product>> {
    return expenses
        .sortedByDescending { it.date }
        .groupBy { expense ->
            try {
                val date = LocalDate.parse(expense.date.substring(0, 10))
                when (timeGroup) {
                    TimeGroup.DAY -> date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                    TimeGroup.WEEK -> {
                        val weekFields = WeekFields.of(Locale.getDefault())
                        val weekNum = date.get(weekFields.weekOfWeekBasedYear())
                        "Week $weekNum, ${date.year}"
                    }
                    TimeGroup.MONTH -> date.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
                    TimeGroup.YEAR -> date.year.toString()
                }
            } catch (_: Exception) {
                "Unknown"
            }
        }
}
