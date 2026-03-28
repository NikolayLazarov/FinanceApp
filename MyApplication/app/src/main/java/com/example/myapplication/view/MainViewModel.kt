package com.example.myapplication.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.models.CreateExpenseRequest
import com.example.myapplication.models.Product
import com.example.myapplication.models.UpdateAllowanceRequest
import com.example.myapplication.services.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TimeGroup { DAY, WEEK, MONTH, YEAR }

class MainViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _expenses = MutableStateFlow<List<Product>>(emptyList())
    val expenses = _expenses.asStateFlow()

    private val _timeGroup = MutableStateFlow(TimeGroup.DAY)
    val timeGroup = _timeGroup.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _isDarkMode = MutableStateFlow<Boolean?>(null) // null means follow system
    val isDarkMode = _isDarkMode.asStateFlow()

    fun loadData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _expenses.value = RetrofitClient.apiService.getExpenses()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setTimeGroup(group: TimeGroup) {
        _timeGroup.value = group
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setDarkMode(enabled: Boolean?) {
        _isDarkMode.value = enabled
    }

    fun addExpense(expense: CreateExpenseRequest, onAllowanceDeducted: (Double) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.createExpense(expense)
                if (response.isSuccessful) {
                    onAllowanceDeducted(expense.amount)
                    _expenses.value = RetrofitClient.apiService.getExpenses()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateExpense(expense: CreateExpenseRequest) {
        viewModelScope.launch {
            try {
                // Using the existing createExpense endpoint as it seems to be CreateOrUpdate
                val response = RetrofitClient.apiService.createExpense(expense)
                if (response.isSuccessful) {
                    _expenses.value = RetrofitClient.apiService.getExpenses()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateAllowance(dailyAllowance: Double, savings: Double) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.updateDailyAllowance(
                    UpdateAllowanceRequest(dailyAllowance, savings)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
