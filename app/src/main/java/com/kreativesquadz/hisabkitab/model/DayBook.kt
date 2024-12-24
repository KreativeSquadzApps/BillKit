package com.kreativesquadz.hisabkitab.model

import androidx.room.PrimaryKey

data class DayBook(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val invoiceNumber: String,
    val createdBy: String,
    val customerName: String,
    val totalAmount: Double,
    val cashAmount: Double? = null,
)



