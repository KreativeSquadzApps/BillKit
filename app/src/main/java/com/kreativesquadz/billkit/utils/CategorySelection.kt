package com.kreativesquadz.billkit.utils

// Sealed class to represent the possible selections
sealed class CategorySelection<out T> {
    object All : CategorySelection<Nothing>() // Representing the "All" selection
    data class SelectedCategory<T>(val category: T) : CategorySelection<T>()
}
