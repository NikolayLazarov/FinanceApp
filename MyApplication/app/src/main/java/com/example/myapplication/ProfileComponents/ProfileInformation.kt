package com.example.myapplication.ProfileComponents

import android.graphics.drawable.Icon
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.components.CardBlock

@Composable
fun PersonalInformation(
    age: Int = 0,
    gender: String = "",
){
    Column(){
        Avatar()
        PersonalDetails("John Ivanov", age, "dummy.email@gmail.com", "0987654321")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
//                InfoRow(label = "Възраст", value = age, icon = Icons.Default.Star )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
//                InfoRow(label = "Пол", value = gender, icon = Icons.Default.Face)
            }
        }

        Row() {
            CardBlock("Daily Amount", 23)
            CardBlock("Current Savings", 1000)

        }
        EditableTextBlock()
    }
}

@Composable
fun InfoRow(label: String, value: String, icon: Icons.Filled) {
    TODO("Not yet implemented")
}

