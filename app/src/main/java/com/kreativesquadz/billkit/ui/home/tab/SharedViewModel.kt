package com.kreativesquadz.billkit.ui.home.tab

import android.view.View
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem


class SharedViewModel : ViewModel() {
    private val _items = MutableLiveData<List<InvoiceItem>>()
    val items: LiveData<List<InvoiceItem>> get() = _items
    var list = mutableListOf<InvoiceItem>()
    private val _selectedCustomer = MutableLiveData<Customer?>()
    val selectedCustomer: LiveData<Customer?>
        get() = _selectedCustomer
    val amountValue: MutableLiveData<String> by lazy {
        MutableLiveData("0")
    }
    val amount: LiveData<String> get() =  amountValue
    var amountBuilder = StringBuilder()



    fun getAmount(view: View){
        val amount = (view as TextView).text ?: ""
        amountBuilder.append(amount.toString())
        amountValue.value = amountBuilder.toString()
    }

    fun delete(){
        if (amountBuilder.isEmpty()){
            return
        }
        amountBuilder.deleteCharAt(amountBuilder.length - 1)
        amountValue.value = amountBuilder.toString()
    }

    fun addItem(){
        if (amountBuilder.toString().isEmpty()){
            return
        }
        val amount = amountBuilder.toString()
        val itemName = "Item${list.size + 1} "

        if (amount.contains("X")){
            val ammountArray =  amount.split("X")
            if (ammountArray.size == 2) {
                val amnt = ammountArray[0]
                val qty = ammountArray[1]
                val include = "X"
                val finalAmount = amnt.replace("X", "").toDouble() * qty.toDouble()
                val homeItem =  InvoiceItem(
                    invoice_id = 1,
                    item_name = "$itemName( $amnt )  $include $qty",
                    unit_price = finalAmount,
                    quantity = qty.toInt(),
                    total_price = finalAmount,
                    tax_rate = 0.10
                )
                list.add(homeItem)
            }
        }else{
            val include = "X"
            val qty = "1"
            val finalAmount = amount.toDouble() * qty.toDouble()
            val homeItem =  InvoiceItem(
                invoice_id = 1,
                item_name = "$itemName( $amountBuilder )  $include $qty",
                unit_price = finalAmount,
                quantity = qty.toInt(),
                total_price = finalAmount,
                tax_rate = 0.10
            )
            list.add(homeItem)
        }
        _items.value = list
        amountBuilder.clear()
        amountValue.value = amountBuilder.toString()
    }

    fun getInvoiceItem(): List<InvoiceItem> {
        return list
    }


    fun getInvoiceItemCount(): String {
        return list.size.toString()
    }

    fun getSubTotalamount(): String {
        var total = 0.0
        list.forEach {
            total += it.total_price!!
        }

        return Config.CURRENCY+total.toString()
    }
    fun getSubTotalamountDouble(): Double {
        var total = 0.0
        list.forEach {
            total += it.total_price!!
        }

        return total
    }

    fun getTotalTax(): String {
        var total = 0.0
        list.forEach {
            total += it.tax_rate!!
        }
        return Config.CURRENCY+total.toString()
    }

    fun getTotalAmount(): String {
        val totalTax = getTotalTax()
            .replace(Config.CURRENCY,"")
            .toDouble() + getSubTotalamount()
            .replace(Config.CURRENCY,"")
            .toDouble()
    return Config.CURRENCY+totalTax.toString()

    }

    fun getTotalAmountDouble(): Double {
        val totalTax = getTotalTax()
            .replace(Config.CURRENCY,"")
            .toDouble() + getSubTotalamount()
            .replace(Config.CURRENCY,"")
            .toDouble()
    return totalTax
    }
    fun updateSelectedCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
    }
    fun getInvoice() : Invoice{
        val invoice = Invoice(
            invoice_number = "INV ${getInvoiceItemCount()}",
            invoice_date = System.currentTimeMillis().toString(),
            invoice_time = System.currentTimeMillis().toString(),
            created_by = "Created By Admin",
            total_items = getInvoiceItem().size,
            subtotal = getSubTotalamountDouble(),
            cash_amount = getTotalAmountDouble(),
            total_amount = getTotalAmountDouble(),
            customerId = getCustomerId(),
            invoice_items = getItemsList()
        )
        return invoice
    }

    fun getItemsList(): List<InvoiceItem> {
        return list
    }

    fun getCustomerId() : Long?{
        val selectedCustomer = _selectedCustomer.value
        val customerId = selectedCustomer?.id ?: 0
        if (customerId == 0L){
            return null
        }
        return customerId
    }




}