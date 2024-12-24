package com.kreativesquadz.hisabkitab.model.request

data class ProductStockRequest(
    val id: Long, // or the unique identifier of your invoice
    val productStock: Int
)