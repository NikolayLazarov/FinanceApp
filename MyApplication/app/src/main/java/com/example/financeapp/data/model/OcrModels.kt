package com.example.financeapp.data.model

data class OcrReceiptResult(
    val storeName: String? = null,
    val items: List<OcrReceiptItem> = emptyList(),
    val total: Double? = null,
    val itemCount: Int = 0,
    val rawText: String? = null,
    val correctedText: String? = null
)

data class OcrReceiptItem(
    val name: String = "",
    val price: Double = 0.0
)
