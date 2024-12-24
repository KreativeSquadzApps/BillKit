package com.kreativesquadz.hisabkitab.interfaces

import com.kreativesquadz.hisabkitab.model.Category

interface OnItemCatListener<T>{
    fun onItemCat(item: Category)
}