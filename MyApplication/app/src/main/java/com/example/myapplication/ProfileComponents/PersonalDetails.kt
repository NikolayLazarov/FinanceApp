package com.example.myapplication.ProfileComponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PersonalDetails(name: String, age: Int, email: String, mobile: String){
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer, // Светло син/лилав фон
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer, // Тъмен текст за контраст
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(20.dp)

    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(text = "Name: $name")
            Text(text = "Age: $age")
            Text(text = "Email: $email")
            Text(text = "Mobile: $mobile")
        }


    }
}