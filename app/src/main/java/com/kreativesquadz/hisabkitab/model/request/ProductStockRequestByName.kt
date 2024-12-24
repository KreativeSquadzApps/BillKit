package com.kreativesquadz.hisabkitab.model.request

data class ProductStockRequestByName(
    val productName: String, // or the unique identifier of your invoice
    val productStock: Int
)