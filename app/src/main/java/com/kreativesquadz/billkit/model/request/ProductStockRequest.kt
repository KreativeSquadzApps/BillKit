package com.kreativesquadz.billkit.model.request

data class ProductStockRequest(
    val id: Long, // or the unique identifier of your invoice
    val productStock: Int
)