package com.example.myapplication.pages.main.common.components

import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Title(title:String, modifier: Modifier = Modifier){
    Text( text = title,
        style = Typography().headlineLarge ,
        color = lightColorScheme().primary)
}