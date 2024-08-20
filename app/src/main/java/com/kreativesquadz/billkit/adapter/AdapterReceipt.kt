package com.kreativesquadz.billkit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kreativesquadz.billkit.databinding.ItemInvoiceItemReceiptBinding
import com.kreativesquadz.billkit.interfaces.OnItemClickListener

class AdapterReceipt<T>(
    private var items: List<T>,
    private val listener: OnItemClickListener<T>,
    private val layoutResId: Int,
    private val bindVariableId: Int,
    private var isShowTax : Boolean,
    private var isShowMrp : Boolean

) : RecyclerView.Adapter<AdapterReceipt.ViewHolder<T>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemInvoiceItemReceiptBinding = DataBindingUtil.inflate(inflater, layoutResId, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        val item = items[position]
        holder.bind(item, listener, bindVariableId, isShowTax, isShowMrp)
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<T>) {
        items = newItems
        notifyDataSetChanged()
    }
    fun deleteItem(position: Int) {
        val itemss = items.toMutableList()
        itemss.removeAt(position)
        items = emptyList()
        items = itemss
        notifyItemRemoved(position)
    }

    class ViewHolder<T>(private val binding: ItemInvoiceItemReceiptBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: T, listener: OnItemClickListener<T>, variableId: Int , isShowTax : Boolean, isShowMrp : Boolean) {
            binding.setVariable(variableId, item)
            binding.root.setOnClickListener { listener.onItemClick(item) }
            if(isShowTax){
                binding.tax.visibility = View.VISIBLE
            }else{
                binding.tax.visibility = View.GONE
            }
            if(isShowMrp){
                binding.mrp.visibility = View.VISIBLE
            }else{
                binding.mrp.visibility = View.GONE
            }
            binding.executePendingBindings()
        }
    }

}

