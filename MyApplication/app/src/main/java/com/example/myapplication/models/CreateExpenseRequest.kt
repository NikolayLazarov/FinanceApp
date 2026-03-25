package com.example.myapplication.models

data class CreateExpenseRequest(
    val id: Int? = null,
    val title: String,
    val category: String,
    val date: String,
    val amount: Double
)
