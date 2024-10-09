package com.kreativesquadz.billkit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.interfaces.OnItemClickListener

class InvoicePrefixNumberAdapter<T>(
    private var items: List<T>,
    private val listener: OnItemClickListener<T>,
    private val deleteListener: OnItemClickListener<T>,
    private val reuseListener: OnItemClickListener<T>,
    private val layoutResId: Int,
    private val bindVariableId: Int
) : RecyclerView.Adapter<InvoicePrefixNumberAdapter.ViewHolder<T>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ViewDataBinding = DataBindingUtil.inflate(inflater, layoutResId, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        val item = items[position]
        holder.bind(item, listener,deleteListener ,reuseListener , bindVariableId)
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<T>) {
        items = newItems.toList()
        notifyDataSetChanged()
    }


    class ViewHolder<T>(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: T, listener: OnItemClickListener<T>,deletelistener: OnItemClickListener<T>,reuselistener: OnItemClickListener<T>, variableId: Int) {
            binding.setVariable(variableId, item)
            binding.root.setOnClickListener { listener.onItemClick(item) }
            binding.root.findViewById<ImageView>(R.id.ivDelete).setOnClickListener { deletelistener.onItemClick(item) }
            binding.root.findViewById<TextView>(R.id.tvReuse).setOnClickListener { reuselistener.onItemClick(item) }
            binding.executePendingBindings()
        }
    }

}

