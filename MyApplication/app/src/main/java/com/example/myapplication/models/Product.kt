package com.example.myapplication.models

data class Product(
    val id: Int,
    val title: String,
    val category: String,
    val date: String,
    val amount: Double,
    val creatorUserId: String? = null,
    val creationDate: String? = null,
    val lastModified: String? = null,
    val deleterUserId: String? = null,
    val deletionTime: String? = null
)
