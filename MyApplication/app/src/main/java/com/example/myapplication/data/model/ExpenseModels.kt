package com.example.myapplication.data.model

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

data class RefreshResponse(
    val token: String
)
