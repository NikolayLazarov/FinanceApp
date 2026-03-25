package com.example.myapplication.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.models.Product
import com.example.myapplication.models.ScannedDocument
import com.example.myapplication.services.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _scannedDocuments = MutableStateFlow<List<ScannedDocument>>(emptyList())
    val scannedDocuments = _scannedDocuments.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _products.value = RetrofitClient.apiService.getProducts()
            } catch (e: Exception) {
                // Handle error if needed
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addScannedDocuments(documents: List<ScannedDocument>) {
        _scannedDocuments.value += documents
    }
}
