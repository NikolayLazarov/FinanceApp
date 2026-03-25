package com.example.myapplication.pages.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapplication.models.LoginResult
import com.example.myapplication.pages.profile.components.PersonalInformation
import com.example.myapplication.pages.main.common.components.Title

@Composable
fun Profile(
    userInfo: LoginResult?,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Title("Personal Info")
        PersonalInformation(
            name = if (userInfo != null) "${userInfo.firstName} ${userInfo.lastName}" else "",
            email = userInfo?.email ?: "",
            age = userInfo?.age ?: 0,
            gender = userInfo?.gender ?: "Unspecified",
            dailyAllowance = userInfo?.dailyAllowance ?: 0.0,
            savings = userInfo?.savings ?: 0.0,
            onLogout = onLogout
        )
    }
}
