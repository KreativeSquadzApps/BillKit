package com.kreativesquadz.hisabkitab.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kreativesquadz.hisabkitab.databinding.ItemSaleReturnBinding
import com.kreativesquadz.hisabkitab.interfaces.OnItemClickListener
import com.kreativesquadz.hisabkitab.interfaces.OnItemListListener
import com.kreativesquadz.hisabkitab.interfaces.OnToastShow
import com.kreativesquadz.hisabkitab.model.InvoiceItem

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
            binding.qtyAdd.setOnClickListener {
                if (ogQty > qty) {
                    qty++
                    invoiceItem.returnedQty = qty
                    binding.etQty.setText("$qty")
                    binding.tvAvailableQty.text = (ogQty - qty).toString()
                    listListener.onItemList(item.copy(returnedQty = qty))
                } else {
                    listenerToast.showToast("No Items Available")
                }
            }

            binding.qtyMinus.setOnClickListener {
                if (qty > 0) {
                    qty--
                    invoiceItem.returnedQty = qty
                    binding.etQty.setText("$qty")
                    binding.tvAvailableQty.text = (ogQty - qty).toString()
                    listListener.onItemList(item.copy(returnedQty = qty))
                } else {
                    listenerToast.showToast("Quantity cannot be less than zero")
                }
            }

            binding.executePendingBindings()
        }
    }
    private fun updateQuantity(qty: Int) {

    }

}

