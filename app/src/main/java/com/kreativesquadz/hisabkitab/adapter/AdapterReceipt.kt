package com.kreativesquadz.hisabkitab.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.kreativesquadz.hisabkitab.databinding.ItemInvoiceItemReceiptBinding
import com.kreativesquadz.hisabkitab.interfaces.OnItemClickListener
import com.kreativesquadz.hisabkitab.model.InvoiceItem
import com.kreativesquadz.hisabkitab.model.settings.TaxOption
import com.kreativesquadz.hisabkitab.ui.bills.ReceiptViewModel
import com.kreativesquadz.hisabkitab.ui.bills.creditNote.creditNoteDetails.creditNoteReceipt.CreditNoteReceiptViewModel
import com.kreativesquadz.hisabkitab.utils.TaxType

class AdapterReceipt<T>(
    private var items: List<T>,
    private val listener: OnItemClickListener<T>,
    private val layoutResId: Int,
    private val bindVariableId: Int,
    private var isShowTax : Boolean,
    private var isShowMrp : Boolean,
    private val viewModel: ViewModel

) : RecyclerView.Adapter<AdapterReceipt.ViewHolder<T>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemInvoiceItemReceiptBinding = DataBindingUtil.inflate(inflater, layoutResId, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        val item = items[position]
        holder.bind(item, listener, bindVariableId, isShowTax, isShowMrp,viewModel)
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
        fun bind(item: T, listener: OnItemClickListener<T>, variableId: Int , isShowTax : Boolean, isShowMrp : Boolean, viewModel : ViewModel) {
            binding.setVariable(variableId, item)
            binding.root.setOnClickListener { listener.onItemClick(item) }
            val invoiceItem = item as InvoiceItem
            var finalRate = invoiceItem.unitPrice
            if (viewModel is ReceiptViewModel) {
                if (invoiceItem.isProduct == 1){
                    invoiceItem.productTaxType?.let { taxTypeString ->
                        val taxType = TaxType.fromString(taxTypeString)
                        taxType?.let { type ->
                            when (type) {
                                TaxType.PriceIncludesTax -> {
                                    val productTax =   item.unitPrice.times(item.taxRate).div(100)
                                    finalRate -= productTax
                                }
                                TaxType.PriceWithoutTax -> {}
                                TaxType.ZeroRatedTax -> {}
                                TaxType.ExemptTax -> {}
                            }
                        }
                    }
                }
                else{
                    val selectedTaxPercentage = viewModel.taxSettings.value?.selectedTaxPercentage
                    Log.d("AdapterReceipt", "Tax Type: $selectedTaxPercentage")
                    viewModel.taxSettings.value?.defaultTaxOption?.let {
                        if (it == TaxOption.PriceIncludesTax){
                            selectedTaxPercentage?.let {
                                val productTax =  invoiceItem.unitPrice.times(it).div(100)
                                finalRate -= productTax
                            }
                        }
                        if (it == TaxOption.PriceExcludesTax){}
                        if (it == TaxOption.ZeroRatedTax){ }
                        if (it == TaxOption.ExemptTax){ }
                    }
                }
                binding.qty.text = item.quantity.toString()
            }
            if (viewModel is CreditNoteReceiptViewModel) {
                if (invoiceItem.isProduct == 1){
                    invoiceItem.productTaxType?.let { taxTypeString ->
                        val taxType = TaxType.fromString(taxTypeString)
                        taxType?.let { type ->
                            when (type) {
                                TaxType.PriceIncludesTax -> {
                                    val productTax =   item.unitPrice.times(item.taxRate).div(100)
                                    finalRate -= productTax
                                }
                                TaxType.PriceWithoutTax -> {}
                                TaxType.ZeroRatedTax -> {}
                                TaxType.ExemptTax -> {}
                            }
                        }
                    }
                }
                else{
                    val selectedTaxPercentage = viewModel.taxSettings.value?.selectedTaxPercentage
                    Log.d("AdapterReceipt", "Tax Type: $selectedTaxPercentage")
                    viewModel.taxSettings.value?.defaultTaxOption?.let {
                        if (it == TaxOption.PriceIncludesTax){
                            selectedTaxPercentage?.let {
                                val productTax =  invoiceItem.unitPrice.times(it).div(100)
                                finalRate -= productTax
                            }
                        }
                        if (it == TaxOption.PriceExcludesTax){}
                        if (it == TaxOption.ZeroRatedTax){ }
                        if (it == TaxOption.ExemptTax){ }
                    }
                }
                binding.qty.text = item.returnedQty.toString()

            }
            binding.rate.text = finalRate.toString()

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

