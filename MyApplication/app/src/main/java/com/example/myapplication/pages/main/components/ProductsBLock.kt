package com.example.myapplication.pages.main.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.models.Product

fun LazyListScope.productsBlock(products: List<Product>) {
    item {
        Text(
            text = "Products",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
    items(products, key = { it.id }) { product ->
        ProductRow(
            product = product,
            onUpdate = { /* Handle update in ViewModel if needed */ }
        )
    }
}
