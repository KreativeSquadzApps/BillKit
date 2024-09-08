package com.kreativesquadz.billkit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kreativesquadz.billkit.interfaces.OnItemClickListener


class GenericAdapterPagination<T : Any>(
    private val listener: OnItemClickListener<T>,
    private val layoutResId: Int,
    private val bindVariableId: Int,
    diffCallback: DiffUtil.ItemCallback<T> // Added DiffUtil for PagingDataAdapter
) : PagingDataAdapter<T, GenericAdapterPagination.ViewHolder<T>>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ViewDataBinding = DataBindingUtil.inflate(inflater, layoutResId, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        val item = getItem(position) // PagingDataAdapter provides this method
        item?.let {
            holder.bind(it, listener, bindVariableId)
        }
    }

    class ViewHolder<T>(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: T, listener: OnItemClickListener<T>, variableId: Int) {
            binding.setVariable(variableId, item)
            binding.root.setOnClickListener { listener.onItemClick(item) }
            binding.executePendingBindings()
        }
    }


}