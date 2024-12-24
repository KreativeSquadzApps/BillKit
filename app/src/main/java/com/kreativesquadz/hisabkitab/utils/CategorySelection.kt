package com.kreativesquadz.hisabkitab.utils

sealed class CategorySelection<out T> {
    object All : CategorySelection<Nothing>() // Representing the "All" selection
    data class SelectedCategory<T>(val category: T) : CategorySelection<T>()
}
