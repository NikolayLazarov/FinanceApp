package com.example.financeapp.ui.features.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.model.CreateExpenseRequest
import com.example.financeapp.data.model.Product
import com.example.financeapp.data.model.TimeGroup
import com.example.financeapp.data.model.UpdateAllowanceRequest
import com.example.financeapp.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: FinanceRepository = FinanceRepository()) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _expenses = MutableStateFlow<List<Product>>(emptyList())
    val expenses = _expenses.asStateFlow()

    private val _timeGroup = MutableStateFlow(TimeGroup.DAY)
    val timeGroup = _timeGroup.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _isDarkMode = MutableStateFlow<Boolean?>(null)
    val isDarkMode = _isDarkMode.asStateFlow()

    fun loadData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _expenses.value = repository.getExpenses()
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
                val response = repository.createExpense(expense)
                if (response.isSuccessful) {
                    onAllowanceDeducted(expense.amount)
                    _expenses.value = repository.getExpenses()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateExpense(expense: CreateExpenseRequest) {
        viewModelScope.launch {
            try {
                val response = repository.createExpense(expense)
                if (response.isSuccessful) {
                    _expenses.value = repository.getExpenses()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteExpense(id: Int) {
        viewModelScope.launch {
            try {
                val response = repository.deleteExpense(id)
                if (response.isSuccessful) {
                    _expenses.value = repository.getExpenses()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateAllowance(dailyAllowance: Double, savings: Double) {
        viewModelScope.launch {
            try {
                repository.updateDailyAllowance(
                    UpdateAllowanceRequest(dailyAllowance, savings)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
