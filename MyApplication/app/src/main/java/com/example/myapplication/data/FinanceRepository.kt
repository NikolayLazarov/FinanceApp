package com.example.myapplication.data

import com.example.myapplication.models.*
import com.example.myapplication.services.RetrofitClient
import retrofit2.Response

class FinanceRepository {
    private val api = RetrofitClient.apiService

    suspend fun getExpenses(): List<Product> = api.getExpenses()
    
    suspend fun getMe(): Response<LoginResult> = api.getMe()
    
    suspend fun signIn(request: LoginRequest): Response<LoginResult> = api.signIn(request)
    
    suspend fun signUp(request: RegisterRequest): Response<String> = api.signUp(request)
    
    suspend fun createExpense(expense: CreateExpenseRequest): Response<Unit> = api.createExpense(expense)
    
    suspend fun deleteExpense(id: Int): Response<Unit> = api.deleteExpense(id)
    
    suspend fun updateDailyAllowance(request: UpdateAllowanceRequest): Response<Unit> = api.updateDailyAllowance(request)
    
    suspend fun revoke(): Response<Unit> = api.revoke()
}
