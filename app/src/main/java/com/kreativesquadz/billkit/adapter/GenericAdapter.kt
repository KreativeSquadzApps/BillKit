package com.kreativesquadz.billkit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.kreativesquadz.billkit.interfaces.OnItemClickListener

class GenericAdapter<T>(
    private var items: List<T>,
    private val listener: OnItemClickListener<T>,
    private val layoutResId: Int,
    private val bindVariableId: Int
) : RecyclerView.Adapter<GenericAdapter.ViewHolder<T>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ViewDataBinding = DataBindingUtil.inflate(inflater, layoutResId, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        val item = items[position]
        holder.bind(item, listener, bindVariableId)
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<T>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder<T>(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: T, listener: OnItemClickListener<T>, variableId: Int) {
            binding.setVariable(variableId, item)
            binding.root.setOnClickListener { listener.onItemClick(item) }
            binding.executePendingBindings()
        }
    }

}

