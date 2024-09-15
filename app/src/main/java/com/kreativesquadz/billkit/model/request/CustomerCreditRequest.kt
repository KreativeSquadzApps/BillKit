package com.kreativesquadz.billkit.model.request

data class CustomerCreditRequest(
    val id: Long, // or the unique identifier of your invoice
    val creditAmount: Double,
    val type : String?
)