package com.example.financeapp.data.model

data class Product(
    val id: Int,
    val title: String,
    val amount: Double,
    val date: String,
    val category: String,
    val creatorUserId: String
)
