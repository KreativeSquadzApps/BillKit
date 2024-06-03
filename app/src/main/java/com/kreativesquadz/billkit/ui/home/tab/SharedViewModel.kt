package com.kreativesquadz.billkit.ui.home.tab

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.model.Product
import java.util.UUID


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
    var _isCustomerSelected = MutableLiveData<Boolean>()
    val isCustomerSelected : LiveData<Boolean> get() = _isCustomerSelected
    val include = "X"

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

                val finalAmount = amnt.replace("X", "").toDouble() * qty.toDouble()
                val homeItem =  InvoiceItem(
                    invoiceId = 1,
                    itemName = "$itemName( $amnt )  $include $qty",
                    unitPrice = amnt.toDouble(),
                    quantity = qty.toInt(),
                    totalPrice = finalAmount,
                    taxRate = 0.10
                )
                list.add(homeItem)
            }
        }else{
            val include = "X"
            val qty = "1"
            val finalAmount = amount.toDouble() * qty.toDouble()
            val homeItem =  InvoiceItem(
                invoiceId = 1,
                itemName = "$itemName( $amountBuilder )  $include $qty",
                unitPrice = amountBuilder.toString().toDouble(),
                quantity = qty.toInt(),
                totalPrice = finalAmount,
                taxRate = 0.10
            )
            list.add(homeItem)
        }
        _items.value = list
        amountValue.value = amountBuilder.clear().toString()
    }

    fun addProduct(product: Product){
        var defaultQty = product.productDefaultQty
        if (defaultQty == 0){
            defaultQty = 1
        }
        val homeItem =  InvoiceItem(
            invoiceId = 1,
            itemName = "${product.productName} ( ${product.productPrice} )  $include ${defaultQty}",
            unitPrice = product.productPrice.toString().toDouble(),
            quantity = product.productDefaultQty.toString().toInt(),
            totalPrice = ((product.productPrice.toString().toDouble() * defaultQty!!) + product.productTax.toString().toDouble()),
            taxRate = product.productTax.toString().toDouble()
        )
        list.add(homeItem)
        _items.value = list
    }

    fun clearOrder(){
        list.clear()
        _items.value = list
        amountValue.value = amountBuilder.clear().toString()
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
            total += it.totalPrice!!
        }

        return Config.CURRENCY+total.toString()
    }
    fun getSubTotalamountDouble(): Double {
        var total = 0.0
        list.forEach {
            total += it.totalPrice!!
        }

        return total
    }

    fun getTotalTax(): String {
        var total = 0.0
        list.forEach {
            total += it.taxRate!!
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
        _isCustomerSelected.value = true
    }
    fun updateDeselectCustomer() {
        _selectedCustomer.value = null
        _isCustomerSelected.value = false
    }

    fun getInvoice() : Invoice{
        val invoice = Invoice(
            invoiceId = generateInvoiceId(),
            invoiceNumber = "INV ${getInvoiceItemCount()}",
            invoiceDate = System.currentTimeMillis().toString(),
            invoiceTime = System.currentTimeMillis().toString(),
            createdBy = "Created By Admin",
            totalItems = getInvoiceItem().size,
            subtotal = getSubTotalamountDouble(),
            cashAmount = getTotalAmountDouble(),
            totalAmount = getTotalAmountDouble(),
            customerId = getCustomerId(),
            isSynced = 0,
            invoiceItems = getInvoiceItem()
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

    fun generateInvoiceId(): Int {
        // Generate a unique invoiceId using a combination of timestamp and counter
        val timestamp = System.currentTimeMillis()
        val counter = (0 until 1000).random() // Choose a random number as the counter
        return (timestamp / 1000).toInt() * 1000 + counter
    }

}
