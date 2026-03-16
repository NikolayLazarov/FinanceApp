package com.example.myapplication.ProfileComponents

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun PersonalDetails(name: String, age: Int, email: String, mobile: String){
    Column() {
        Text(text = "Name: $name")
        Text(text = "Age: $age")
        Text(text = "Email: $email")
        Text(text = "Mobile: $mobile")

    }
}