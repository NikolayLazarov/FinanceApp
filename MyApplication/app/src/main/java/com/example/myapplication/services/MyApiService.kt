package com.example.myapplication.services
import com.example.myapplication.models.Product
import retrofit2.http.GET


interface MyApiService {
    @GET("Expenses/GetExpenses")
    suspend fun getProducts(): List<Product> // Използвайте suspend за Coroutines
}
