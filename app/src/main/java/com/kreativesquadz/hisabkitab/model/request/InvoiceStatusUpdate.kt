package com.kreativesquadz.hisabkitab.model.request

data class InvoiceStatusUpdate(
    val invoiceId: Long,
    val status: String
)