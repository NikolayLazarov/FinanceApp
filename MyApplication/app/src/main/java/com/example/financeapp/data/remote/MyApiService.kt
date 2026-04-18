package com.example.financeapp.data.remote

import com.example.financeapp.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MyApiService {
    @POST("Authentication/SignIn")
    suspend fun signIn(@Body request: LoginRequest): Response<LoginResult>

    @POST("Authentication/SignUp")
    suspend fun signUp(@Body request: RegisterRequest): Response<String>

    @POST("Authentication/Refresh")
    suspend fun refresh(): Response<RefreshResponse>

    @POST("Authentication/Revoke")
    suspend fun revoke(): Response<Unit>

    @GET("Authentication/Me")
    suspend fun getMe(): Response<LoginResult>

    @GET("Expenses/GetExpenses")
    suspend fun getExpenses(): List<Product>

    @POST("Expenses/CreateOrUpdateExpense")
    suspend fun createExpense(@Body request: CreateExpenseRequest): Response<Unit>

    @POST("Expenses/DeleteExpense")
    suspend fun deleteExpense(@Body request: DeleteExpenseRequest): Response<Unit>

    @POST("Expenses/UpdateDailyAllowance")
    suspend fun updateDailyAllowance(@Body request: UpdateAllowanceRequest): Response<Unit>

    @POST("Expenses/RefreshDailyBudget")
    suspend fun refreshDailyBudget(@Body request: RefreshDailyBudgetRequest): Response<RefreshDailyBudgetResponse>

    @Multipart
    @POST("Ocr/ScanReceipt")
    suspend fun scanReceipt(@Part file: MultipartBody.Part): Response<OcrReceiptResult>
}
