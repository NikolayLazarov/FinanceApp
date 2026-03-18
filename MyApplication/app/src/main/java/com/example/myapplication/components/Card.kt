package com.example.myapplication.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun CardContainer( title:String, value: String, modifier: Modifier = Modifier){
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp // Връща широчината в dp

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer, // Светло син/лилав фон
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer, // Тъмен текст за контраст
        shape = MaterialTheme.shapes.small,
        modifier = modifier.padding(20.dp).height(120.dp)
    ) {
        Column(
            // Трябва да има височина, за да центрира спрямо нея
            verticalArrangement = Arrangement.Center, // Центрира елементите вертикално
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(start = 10.dp, top = 10.dp),
                text = title,
                style = Typography().titleLarge,
                color = lightColorScheme().secondary

            )
            Text(
                modifier = Modifier.padding(10.dp),
                text = value,
                style = Typography().titleLarge ,
                color = lightColorScheme().primary
            )
        }

    }
}