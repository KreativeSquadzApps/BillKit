package com.kreativesquadz.billkit.model.request

import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem

data class InvoiceRequest(
    val invoice: Invoice,
    val invoiceItems: List<InvoiceItem>
)
