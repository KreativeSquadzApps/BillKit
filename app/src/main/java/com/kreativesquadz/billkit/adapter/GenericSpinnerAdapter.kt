package com.kreativesquadz.billkit.adapter

import android.content.Context
import android.widget.ArrayAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData

class GenericSpinnerAdapter<T>(
    context: Context,
    private val layoutResId: Int,
    private val bindVariableId: Int,
    private val staticItems: List<T>? = null,
    private val liveDataItems: LiveData<List<T>>? = null
) : ArrayAdapter<T>(context, layoutResId) {

    private var itemList: List<T>? = null // Maintain a reference to the list

    init {
        staticItems?.let { items ->
            itemList = items
        }

        liveDataItems?.observeForever { newList ->
            itemList = newList
            notifyDataSetChanged() // Notify adapter when data changes
        }
    }

    override fun getCount(): Int {
        return itemList?.size ?: 0 // Return size of itemList if not null
    }

    override fun getItem(position: Int): T? {
        return itemList?.getOrNull(position) // Get item at position if itemList is not null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent)
    }

    private fun createViewFromResource(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: ViewDataBinding = if (convertView == null) {
            DataBindingUtil.inflate(LayoutInflater.from(context), layoutResId, parent, false)
        } else {
            DataBindingUtil.getBinding(convertView) ?: DataBindingUtil.inflate(LayoutInflater.from(context), layoutResId, parent, false)
        }

        binding.setVariable(bindVariableId, getItem(position))
        binding.executePendingBindings()
        return binding.root
    }
}
