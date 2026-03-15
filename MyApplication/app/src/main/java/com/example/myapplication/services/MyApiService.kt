package com.example.myapplication.services
import com.example.myapplication.Product
import com.example.myapplication.User
import retrofit2.http.GET


interface MyApiService {
    @GET("Expenses/GetExpenses")
    suspend fun getProducts(): List<Product> // Използвайте suspend за Coroutines
}
