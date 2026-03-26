package com.example.myapplication.models

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DummyData {
    private val categories = listOf(
        "Food", "Transportation", "Utilities", "Healthcare", "Education",
        "Entertainment", "Shopping", "Travel", "Finance", "Other"
    )

    val dummyExpenses: List<Product> by lazy {
        val expenses = mutableListOf<Product>()
        val now = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        // Generate data for the last 2 years to cover all TimeGroups
        for (i in 0 until 730) {
            val date = now.minusDays(i.toLong())
            // Add 1-3 expenses per day
            val count = (1..3).random()
            repeat(count) {
                val category = categories.random()
                val amount = when (category) {
                    "Food" -> (5..50).random().toDouble()
                    "Transportation" -> (10..100).random().toDouble()
                    "Utilities" -> (50..200).random().toDouble()
                    "Travel" -> (200..1000).random().toDouble()
                    else -> (1..150).random().toDouble()
                }
                
                expenses.add(
                    Product(
                        id = expenses.size + 1,
                        title = "$category Expense",
                        category = category,
                        date = date.atTime((8..22).random(), (0..59).random()).format(formatter),
                        amount = amount
                    )
                )
            }
        }
        expenses
    }
}
