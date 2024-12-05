package com.kreativesquadz.billkit.model.request

data class InvoiceStatusUpdate(
    val invoiceId: Long,
    val status: String
)