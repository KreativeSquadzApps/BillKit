package com.kreativesquadz.billkit.interfaces

import com.kreativesquadz.billkit.model.Category

interface OnItemClickListener<T>{
    fun onItemClick(item: T)
}