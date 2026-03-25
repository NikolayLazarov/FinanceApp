package com.example.myapplication.models

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val age: Int,
    val gender: Int,
    val email: String,
    val password: String
)
