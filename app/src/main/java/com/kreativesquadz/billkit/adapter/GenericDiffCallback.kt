package com.kreativesquadz.billkit.adapter

import androidx.recyclerview.widget.DiffUtil

class GenericDiffCallback<T : Any>(
    private val areItemsSame: (T, T) -> Boolean,
    private val areContentsSame: (T, T) -> Boolean
) : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return areItemsSame(oldItem, newItem)  // Compare based on unique identifiers
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return areContentsSame(oldItem, newItem)
    }
}
