package com.kreativesquadz.hisabkitab.model.request

import com.kreativesquadz.hisabkitab.model.Invoice
import com.kreativesquadz.hisabkitab.model.InvoiceItem

data class InvoiceRequest(
    val invoice: Invoice,
    val invoiceItems: List<InvoiceItem>
)
