package com.kreativesquadz.billkit.model

import androidx.room.Entity
import androidx.room.PrimaryKey


data class SavedOrder(
    val orderId: Long,
    val orderName: String,
    val items: List<InvoiceItem> ?= null,
    val totalAmount: Double,
    val date: Long
)
