package com.example.myapplication.pages

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapplication.ProfileComponents.PersonalInformation
import com.example.myapplication.components.Title


@Composable
fun Profile(modifier: Modifier = Modifier){
    Title("Personal Info")
    PersonalInformation()
}