package com.example.myapplication.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun CardBlock(textVal:String, amount: Int){
    Column {
        Text(text = "$textVal: $amount")
    }
}