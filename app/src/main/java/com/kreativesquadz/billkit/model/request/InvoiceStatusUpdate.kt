package com.kreativesquadz.billkit.model.request

data class InvoiceStatusUpdate(
    val invoiceId: Long, // or the unique identifier of your invoice
    val status: String
)