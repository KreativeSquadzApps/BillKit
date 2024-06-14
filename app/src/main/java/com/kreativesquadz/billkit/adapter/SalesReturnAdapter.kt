package com.kreativesquadz.billkit.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kreativesquadz.billkit.databinding.ItemSaleReturnBinding
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.interfaces.OnItemListListener
import com.kreativesquadz.billkit.interfaces.OnToastShow
import com.kreativesquadz.billkit.model.InvoiceItem

class SalesReturnAdapter<T>(
    private var items: List<T>,
    private val listener: OnItemClickListener<T>,
    private val listListener: OnItemListListener<T>,
    private val listenerToast: OnToastShow,
    private val layoutResId: Int,
    private val bindVariableId: Int,



    ) : RecyclerView.Adapter<SalesReturnAdapter.ViewHolder<T>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemSaleReturnBinding = DataBindingUtil.inflate(inflater, layoutResId, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        val item = items[position]
        holder.bind(item, listener,listListener ,bindVariableId, listenerToast)

    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<T>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder<T>(private val binding: ItemSaleReturnBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: T, listener: OnItemClickListener<T>,listListener: OnItemListListener<T>,
                 variableId: Int , listenerToast: OnToastShow) {

            val itemList = mutableListOf<InvoiceItem>()
            val invoiceItem = item as InvoiceItem
            val ogQty = invoiceItem.quantity - invoiceItem.returnedQty!!

            binding.setVariable(variableId, item)
            binding.root.setOnClickListener { listener.onItemClick(item) }
            binding.etQty.setText("0")
            binding.tvAvailableQty.text = ogQty.toString()

            var qty = 0
            binding.qtyAdd.setOnClickListener{
                qty++
                if (ogQty < qty){
                    listenerToast.showToast("No Items Available")
                }else{
                    binding.tvAvailableQty.text = (binding.tvAvailableQty.text.toString().toInt() - 1).toString()
                    binding.etQty.setText("$qty")
                    invoiceItem.returnedQty =  invoiceItem.returnedQty!! + 1
                    listListener.onItemList(item.copy(returnedQty = invoiceItem.returnedQty))
                }
            }
            binding.qtyMinus.setOnClickListener {
                if (qty > 0) {
                    qty--
                    binding.tvAvailableQty.text = (binding.tvAvailableQty.text.toString().toInt() + 1).toString()
                    binding.etQty.setText("$qty")
                    invoiceItem.returnedQty = invoiceItem.returnedQty!! - 1
                    listListener.onItemList(item.copy(returnedQty = invoiceItem.returnedQty))
                } else {
                    // Optionally, show a toast if trying to decrement below zero
                    listenerToast.showToast("Quantity cannot be less than zero")
                }
            }
            binding.executePendingBindings()
        }
    }

}

