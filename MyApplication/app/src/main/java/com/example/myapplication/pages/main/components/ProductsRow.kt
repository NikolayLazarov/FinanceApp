package com.example.myapplication.pages.main.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myapplication.models.Product

@Composable
fun ProductRow(product: Product, onUpdate: (Product) -> Unit) {

    var title by remember { mutableStateOf(product.title) }
    var amount by remember { mutableStateOf(product.amount.toString()) }

    Row {
        TextField(
            value = title,
            onValueChange = {
                title = it
                onUpdate(product.copy(title = it))
            }
        )

        TextField(
            value = amount,
            onValueChange = {
                amount = it
                onUpdate(product.copy(amount = it.toDoubleOrNull() ?: 0.0))
            }
        )
    }
}