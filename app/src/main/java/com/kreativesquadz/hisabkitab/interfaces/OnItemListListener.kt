package com.kreativesquadz.hisabkitab.interfaces

import com.kreativesquadz.hisabkitab.model.InvoiceItem

interface OnItemListListener<T>{
    fun onItemList(item: InvoiceItem)
}