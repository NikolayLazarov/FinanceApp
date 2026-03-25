package com.example.myapplication.pages.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PersonalDetails(name: String, age: Int, email: String, gender: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(text = "Name: $name")
            Text(text = "Age: $age")
            Text(text = "Email: $email")
            Text(text = "Gender: $gender")
        }
    }
}
