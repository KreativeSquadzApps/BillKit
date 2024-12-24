package com.kreativesquadz.hisabkitab.model.request

data class CreditNoteStatusUpdate(
    val id: Int, // or the unique identifier of your invoice
    val status: String
)