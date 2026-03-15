package com.example.myapplication.services

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Файл: RetrofitClient.kt
object RetrofitClient {
    private const val BASE_URL = "http://10.185.204.45:5062/"

    val apiService: MyApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MyApiService::class.java)
    }
}
