package com.example.myapplication.pages.main.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RemainingBlock(modifier: Modifier = Modifier){
    var dailyAmount = 10
    var spentAmount = 3.99
    var currentAmount = dailyAmount - spentAmount

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer, // Светло син/лилав фон
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer, // Тъмен текст за контраст
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(20.dp)
    ) {
        Text(
            text = "Remaining for today",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            style = MaterialTheme.typography.labelMedium,
            color = lightColorScheme().secondary
        )
        Text(text = "$currentAmount E",
            modifier = Modifier.padding(horizontal = 60.dp, vertical = 40.dp),

            style = Typography().labelMedium,
            color = lightColorScheme().secondary
        )
    }
}