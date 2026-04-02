package com.example.financeapp.data.repository

import com.example.financeapp.data.model.*
import com.example.financeapp.data.remote.RetrofitClient
import retrofit2.Response

class FinanceRepository {
    private val api = RetrofitClient.apiService

    suspend fun getExpenses(): List<Product> = api.getExpenses()
    
    suspend fun getMe(): Response<LoginResult> = api.getMe()
    
    suspend fun signIn(request: LoginRequest): Response<LoginResult> = api.signIn(request)
    
    suspend fun signUp(request: RegisterRequest): Response<String> = api.signUp(request)
    
    suspend fun createExpense(expense: CreateExpenseRequest): Response<Unit> = api.createExpense(expense)
    
    suspend fun deleteExpense(id: Int): Response<Unit> = api.deleteExpense(DeleteExpenseRequest(id))
    
    suspend fun updateDailyAllowance(request: UpdateAllowanceRequest): Response<Unit> = api.updateDailyAllowance(request)
    
    suspend fun revoke(): Response<Unit> = api.revoke()

    suspend fun refreshDailyBudget(request: RefreshDailyBudgetRequest): Response<RefreshDailyBudgetResponse> =
        api.refreshDailyBudget(request)
}
