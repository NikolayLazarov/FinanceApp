package com.example.myapplication.services

import com.example.myapplication.models.CreateExpenseRequest
import com.example.myapplication.models.LoginRequest
import com.example.myapplication.models.LoginResult
import com.example.myapplication.models.Product
import com.example.myapplication.models.RegisterRequest
import com.example.myapplication.models.UpdateAllowanceRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MyApiService {
    // Auth
    @POST("Authentication/SignIn")
    suspend fun signIn(@Body request: LoginRequest): Response<LoginResult>

    @POST("Authentication/SignUp")
    suspend fun signUp(@Body request: RegisterRequest): Response<String>

    @POST("Authentication/Refresh")
    suspend fun refresh(): Response<com.example.myapplication.models.RefreshResponse>

    @POST("Authentication/Revoke")
    suspend fun revoke(): Response<Unit>

    @GET("Authentication/Me")
    suspend fun getMe(): Response<LoginResult>

    // Expenses
    @GET("Expenses/GetExpenses")
    suspend fun getExpenses(): List<Product>

    @POST("Expenses/CreateOrUpdateExpense")
    suspend fun createExpense(@Body request: CreateExpenseRequest): Response<Unit>

    @POST("Expenses/DeleteExpense")
    suspend fun deleteExpense(@Query("id") id: Int): Response<Unit>

    @POST("Expenses/UpdateDailyAllowance")
    suspend fun updateDailyAllowance(@Body request: UpdateAllowanceRequest): Response<Unit>
}
