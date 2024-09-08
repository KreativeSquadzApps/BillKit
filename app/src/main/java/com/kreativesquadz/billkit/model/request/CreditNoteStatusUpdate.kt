package com.kreativesquadz.billkit.model.request

data class CreditNoteStatusUpdate(
    val id: Int, // or the unique identifier of your invoice
    val status: String
)