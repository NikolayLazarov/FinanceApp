package com.example.myapplication.pages.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapplication.pages.profile.components.PersonalInformation
import com.example.myapplication.pages.main.common.components.Title


@Composable
fun Profile(modifier: Modifier = Modifier){
    Title("Personal Info")
    PersonalInformation()
}