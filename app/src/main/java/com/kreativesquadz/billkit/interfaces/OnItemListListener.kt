package com.kreativesquadz.billkit.interfaces

import com.kreativesquadz.billkit.model.InvoiceItem

interface OnItemListListener<T>{
    fun onItemList(item: InvoiceItem)
}