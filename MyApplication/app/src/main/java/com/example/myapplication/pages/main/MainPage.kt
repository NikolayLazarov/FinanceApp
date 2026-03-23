package com.example.myapplication.pages.main

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapplication.models.Product
import com.example.myapplication.pages.main.components.ProductsBlock
import com.example.myapplication.pages.main.components.RemainingBlock
import com.example.myapplication.pages.main.common.components.Title

@Composable
fun MainPage(products: List<Product>, modifier: Modifier = Modifier){
    Column() {
        Title("Today's Expenses")
        RemainingBlock()
        ProductsBlock(products)
    }
}