package com.example.myapplication.ProfileComponents

import android.graphics.drawable.Icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.components.CardBlock
import com.example.myapplication.components.CardContainer

@Composable
fun PersonalInformation(
    age: Int = 0,
    gender: String = "",
){
    val dailyAmount = 23;
    val currentAmount = 1000;
    Column(){

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center, // Това центрира елементите хоризонтално
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.user),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(8.dp))

            PersonalDetails("John Ivanov", age, "dummy.email@gmail.com", "0987654321")

        }


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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            CardContainer("Daily Amount", dailyAmount.toString(), modifier = Modifier.weight(1f));
            CardContainer("Current Savings", currentAmount.toString(), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, icon: Icons.Filled) {
    TODO("Not yet implemented")
}

