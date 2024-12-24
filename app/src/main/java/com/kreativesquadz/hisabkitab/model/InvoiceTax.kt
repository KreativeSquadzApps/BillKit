package com.kreativesquadz.hisabkitab.model

data class InvoiceTax(
    var taxType: String,
    var taxableAmount: Double,
    val rate: Double,
    var taxAmount: Double
)
