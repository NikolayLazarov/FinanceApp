package com.example.financeapp.data.model

data class CreateExpenseRequest(
    val id: Int? = null,
    val title: String,
    val category: String,
    val date: String,
    val amount: Double
)

data class UpdateAllowanceRequest(
    val dailyAllowance: Double,
    val savings: Double
)

data class DeleteExpenseRequest(
    val id: Int
)

data class RefreshResponse(
    val token: String
)

data class RefreshDailyBudgetRequest(
    val lastRefreshDate: String
)

data class RefreshDailyBudgetResponse(
    val dailyAllowance: Double,
    val lastRefreshDate: String
)
