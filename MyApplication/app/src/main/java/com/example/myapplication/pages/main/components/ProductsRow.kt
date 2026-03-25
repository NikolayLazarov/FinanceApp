package com.example.myapplication.pages.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.models.Product

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
