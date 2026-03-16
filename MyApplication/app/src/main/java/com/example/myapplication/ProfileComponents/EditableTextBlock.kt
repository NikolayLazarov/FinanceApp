package com.example.myapplication.ProfileComponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EditableTextBlock() {

    var text by remember { mutableStateOf("This is my profile description.") }
    var isEditing by remember { mutableStateOf(false) }

    Column {

        if (isEditing) {

            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth()
            )

            Row {
                Button(
                    onClick = { isEditing = false }
                ) {
                    Text("Save")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { isEditing = false }
                ) {
                    Text("Cancel")
                }
            }

        } else {

            Text(
                text = text,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Button(
                onClick = { isEditing = true }
            ) {
                Text("Edit")
            }
        }
    }
}