package com.example.myapplication.models

data class LoginResult(
    val token: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val age: Int,
    val gender: String,
    val dailyAllowance: Double,
    val savings: Double
)
