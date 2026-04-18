package com.example.financeapp.ui.features.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.financeapp.data.model.*
import com.example.financeapp.ui.features.main.components.AddExpenseDialog
import com.example.financeapp.ui.features.main.components.FinanceCard
import com.example.financeapp.ui.localization.AppStrings
import com.example.financeapp.ui.localization.LocalStrings
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

private val expenseCategories = listOf(
    "Food", "Transportation", "Utilities", "Healthcare", "Education",
    "Entertainment", "Shopping", "Travel", "Finance", "Other"
)

@Composable
fun MainPage(
    expenses: List<Product> = emptyList(),
    timeGroup: TimeGroup = TimeGroup.DAY,
    selectedCategory: String? = null,
    userInfo: LoginResult? = null,
    onTimeGroupChange: (TimeGroup) -> Unit = {},
    onCategoryChange: (String?) -> Unit = {},
    onUpdateExpense: (CreateExpenseRequest) -> Unit = {},
    onDeleteExpense: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    var editingExpense by remember { mutableStateOf<Product?>(null) }
    var expenseToDelete by remember { mutableStateOf<Product?>(null) }

    val filteredExpenses = if (selectedCategory != null) {
        expenses.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    } else {
        expenses
    }

    val grouped = groupExpenses(filteredExpenses, timeGroup, strings)
    val totalSpent = expenses.sumOf { it.amount }

    if (expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = {
                Text(
                    strings.deleteExpense,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(String.format(strings.deleteConfirmFormat, expenseToDelete!!.title))
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteExpense(expenseToDelete!!.id)
                        expenseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    if (editingExpense != null) {
        AddExpenseDialog(
            expenseToEdit = editingExpense,
            onDismiss = { editingExpense = null },
            onConfirm = { request ->
                onUpdateExpense(request)
                editingExpense = null
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "${strings.welcomeBackGreeting}${if (userInfo?.firstName != null) ", ${userInfo.firstName}" else ""}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = strings.yourFinances,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FinanceCard(
                    title = strings.dailyBudget,
                    value = "$${String.format("%.2f", userInfo?.dailyAllowance ?: 0.0)}",
                    icon = Icons.Outlined.AccountBalanceWallet,
                    gradientColors = if ((userInfo?.dailyAllowance ?: 0.0) < 0) listOf(
                        MaterialTheme.colorScheme.error,
                        MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    ) else listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.weight(1f)
                )
                FinanceCard(
                    title = strings.savings,
                    value = "$${String.format("%.2f", userInfo?.savings ?: 0.0)}",
                    icon = Icons.Outlined.Savings,
                    gradientColors = listOf(
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.TrendingDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.totalSpent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "$${String.format("%.2f", totalSpent)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        item {
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
                                strings.timeGroupDisplayName(group),
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
        }

        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onCategoryChange(null) },
                        label = {
                            Text(strings.all, style = MaterialTheme.typography.labelMedium)
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
                            Text(
                                strings.categoryDisplayName(category),
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                        )
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        if (grouped.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.noExpensesYet,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            grouped.forEach { (label, expensesInGroup) ->
                item {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
                items(expensesInGroup, key = { it.id }) { expense ->
                    ExpenseRow(
                        expense = expense,
                        onClick = { editingExpense = expense },
                        onDelete = { expenseToDelete = expense }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(
    expense: Product,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalStrings.current
    val categoryIcon = getCategoryEmoji(expense.category)
    val formattedDate = try {
        val ld = LocalDate.parse(expense.date.substring(0, 10))
        "${ld.dayOfMonth} ${ld.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)}"
    } catch (_: Exception) {
        expense.date.take(10)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = categoryIcon, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${strings.categoryDisplayName(expense.category)} · $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "-$${String.format("%.2f", expense.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = strings.delete,
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun getCategoryEmoji(category: String): String {
    return when (category.lowercase()) {
        "food" -> "\uD83C\uDF54"
        "transportation" -> "\uD83D\uDE97"
        "utilities" -> "\uD83D\uDCA1"
        "healthcare" -> "\uD83C\uDFE5"
        "education" -> "\uD83D\uDCDA"
        "entertainment" -> "\uD83C\uDFAC"
        "shopping" -> "\uD83D\uDED2"
        "travel" -> "\u2708\uFE0F"
        "finance" -> "\uD83C\uDFE6"
        else -> "\uD83D\uDCB0"
    }
}

private fun groupExpenses(
    expenses: List<Product>,
    timeGroup: TimeGroup,
    strings: AppStrings
): List<Pair<String, List<Product>>> {
    if (expenses.isEmpty()) return emptyList()

    return expenses
        .sortedByDescending { it.date }
        .groupBy { expense ->
            try {
                val date = LocalDate.parse(expense.date.substring(0, 10))
                when (timeGroup) {
                    TimeGroup.DAY -> date.toString()
                    TimeGroup.WEEK -> {
                        val weekFields = WeekFields.of(Locale.getDefault())
                        val week = date.get(weekFields.weekOfWeekBasedYear())
                        String.format(strings.weekLabelFormat, week, date.year)
                    }
                    TimeGroup.MONTH -> "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.year}"
                    TimeGroup.YEAR -> "${date.year}"
                }
            } catch (_: Exception) {
                strings.categoryOther
            }
        }
        .toList()
}
