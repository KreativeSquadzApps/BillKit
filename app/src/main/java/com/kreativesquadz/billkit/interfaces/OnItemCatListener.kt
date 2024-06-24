package com.kreativesquadz.billkit.interfaces

import com.kreativesquadz.billkit.model.Category

interface OnItemCatListener<T>{
    fun onItemCat(item: Category)
}