package com.example.myapplication.pages.main.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import com.example.myapplication.models.Product

@Composable
fun ProductsBlock(products: List<Product>){
    val productsMutable = products.toMutableList()

    LazyColumn {
        items(productsMutable, key = { it.id }) { product ->
            ProductRow(product,
                onUpdate = { updatedProduct ->
                    val index = products.indexOfFirst { it.id == updatedProduct.id }
                    if (index != -1) {
                        productsMutable[index] = updatedProduct
                    }
                }
            )
        }
    }
}
