package com.kreativesquadz.hisabkitab.model


data class SavedOrder(
    val orderId: Long,
    val orderName: String,
    val items: List<InvoiceItem> ?= null,
    val totalAmount: Double,
    val date: Long
)
