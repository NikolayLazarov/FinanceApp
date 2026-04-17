package com.example.financeapp.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

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

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val age: Int,
    val gender: Int,
    val email: String,
    val password: String
)

