package com.example.financeapp.ui.features.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.financeapp.data.model.Product

@Composable
fun ProductRow(product: Product, onUpdate: (Product) -> Unit) {
    var title by remember { mutableStateOf(product.title) }
    var amount by remember { mutableStateOf(product.amount.toString()) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    onUpdate(product.copy(title = it))
                },
                label = { Text("Product Name") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it
                    val newAmount = it.toDoubleOrNull() ?: 0.0
                    onUpdate(product.copy(amount = newAmount))
                },
                label = { Text("Price") },
                modifier = Modifier.width(100.dp),
                prefix = { Text("$") },
                singleLine = true
            )
        }
    }
}
