package com.example.myapplication.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapplication.Product
import com.example.myapplication.MainPageCOmponents.ProductsBlock
import com.example.myapplication.MainPageCOmponents.RemainingBlock
import com.example.myapplication.components.Title

@Composable
fun MainPage(products: List<Product>, modifier: Modifier = Modifier){
    Column() {
        Title("Today's Expenses")
        RemainingBlock()
        ProductsBlock(products)
    }
}