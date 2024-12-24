package com.kreativesquadz.hisabkitab.model.request

data class CustomerCreditRequest(
    val id: Long, // or the unique identifier of your invoice
    val creditAmount: Double,
    val type : String?
)