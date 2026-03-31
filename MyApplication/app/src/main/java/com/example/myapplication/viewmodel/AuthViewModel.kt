package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FinanceRepository
import com.example.myapplication.models.LoginRequest
import com.example.myapplication.models.LoginResult
import com.example.myapplication.models.RegisterRequest
import com.example.myapplication.services.RetrofitClient
import com.example.myapplication.services.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: FinanceRepository = FinanceRepository()) : ViewModel() {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _userInfo = MutableStateFlow<LoginResult?>(null)
    val userInfo = _userInfo.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isRestoringSession = MutableStateFlow(true)
    val isRestoringSession = _isRestoringSession.asStateFlow()

    init {
        RetrofitClient.onSessionExpired = {
            _userInfo.value = null
            _isLoggedIn.value = false
        }
    }

    fun tryRestoreSession() {
        if (TokenManager.token == null) {
            _isRestoringSession.value = false
            return
        }

        viewModelScope.launch {
            try {
                repository.getExpenses()

                val saved = TokenManager.userInfo
                if (saved != null) {
                    _userInfo.value = saved
                } else {
                    val meResponse = repository.getMe()
                    if (meResponse.isSuccessful) {
                        val info = meResponse.body()
                        TokenManager.userInfo = info
                        _userInfo.value = info
                    }
                }

                _isLoggedIn.value = true
            } catch (_: Exception) {
                TokenManager.clear()
                RetrofitClient.clearCookies()
                _isLoggedIn.value = false
            } finally {
                _isRestoringSession.value = false
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.signIn(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val result = response.body()!!
                    TokenManager.token = result.token
                    TokenManager.userInfo = result
                    _userInfo.value = result
                    _isLoggedIn.value = true
                } else {
                    _error.value = response.errorBody()?.string() ?: "Login failed"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Network error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUp(
        firstName: String,
        lastName: String,
        age: Int,
        gender: Int,
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.signUp(
                    RegisterRequest(firstName, lastName, age, gender, email, password)
                )
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    _error.value = response.errorBody()?.string() ?: "Registration failed"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Network error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                repository.revoke()
            } catch (_: Exception) { }
            TokenManager.clear()
            RetrofitClient.clearCookies()
            _userInfo.value = null
            _isLoggedIn.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun updateAllowance(dailyAllowance: Double, savings: Double) {
        val updated = _userInfo.value?.copy(
            dailyAllowance = dailyAllowance,
            savings = savings
        )
        _userInfo.value = updated
        TokenManager.userInfo = updated
    }
}
