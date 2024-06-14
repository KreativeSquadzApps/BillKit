package com.kreativesquadz.billkit.ui.home.tab

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.model.CreditNote
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.model.Product
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.UUID


class SharedViewModel : ViewModel() {
    private val _items = MutableLiveData<List<InvoiceItem>>()
    val items: LiveData<List<InvoiceItem>> get() = _items
    var list = mutableListOf<InvoiceItem>()
    private val _selectedCustomer = MutableLiveData<Customer?>()
    val selectedCustomer: LiveData<Customer?> get() = _selectedCustomer

    private val _selectedCreditNote = MutableLiveData<CreditNote?>()
    val selectedCreditNote: LiveData<CreditNote?> get() = _selectedCreditNote

    val amountValue: MutableLiveData<String> by lazy {
        MutableLiveData("0")
    }
    var _isCustomerSelected = MutableLiveData<Boolean>()
    val isCustomerSelected : LiveData<Boolean> get() = _isCustomerSelected
    val include = "X"

    val amount: LiveData<String> get() = amountValue


    var amountBuilder = StringBuilder()
    private var discounted : Int? = null
    private var creditNoteAmount : Int? = null

    var _isDiscountApplied = MutableLiveData<Boolean>()
    val isDiscountApplied : LiveData<Boolean> get() = _isDiscountApplied

    var _isCreditNoteApplied = MutableLiveData<Boolean>()
    val isCreditNoteApplied : LiveData<Boolean> get() = _isCreditNoteApplied


    var _totalLivedata = MutableLiveData<String>()
    val totalLivedata : LiveData<String> get() = _totalLivedata
    val df = DecimalFormat("#")
    var creditNoteId : Int?=0

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
                val invoiceId =  generateInvoiceId().toLong()
                val homeItem =  InvoiceItem(
                    invoiceId = invoiceId,
                    itemName = "$itemName( $amnt )  $include $qty",
                    unitPrice = amnt.toDouble(),
                    quantity = qty.toInt(),
                    returnedQty = 0,
                    totalPrice = finalAmount,
                    taxRate = 0.10
                )
                list.add(homeItem)
            }
        }else{
            val include = "X"
            val qty = "1"
            val finalAmount = amount.toDouble() * qty.toDouble()
            val invoiceId =  generateInvoiceId().toLong()
            val homeItem =  InvoiceItem(
                invoiceId = invoiceId,
                itemName = "$itemName( $amountBuilder )  $include $qty",
                unitPrice = amountBuilder.toString().toDouble(),
                quantity = qty.toInt(),
                returnedQty = 0,
                totalPrice = finalAmount,
                taxRate = 0.10
            )
            list.add(homeItem)
        }
        _items.value = list
        amountValue.value = amountBuilder.clear().toString()
    }


    fun addProduct(product: Product?){
        if (product == null){
            return
        }
        var defaultQty = product.productDefaultQty
        if (defaultQty == 0){
            defaultQty = 1
        }
      val invoiceId =  generateInvoiceId().toLong()
        val homeItem =  InvoiceItem(
            invoiceId = invoiceId,
            itemName = "${product.productName} ( ${product.productPrice} )  $include ${defaultQty}",
            unitPrice = product.productPrice.toString().toDouble(),
            quantity = defaultQty!!.toInt(),
            returnedQty = 0,
            totalPrice = ((product.productPrice.toString().toDouble() * defaultQty) + product.productTax.toString().toDouble()),
            taxRate = product.productTax.toString().toDouble()
        )
        list.add(homeItem)
        _items.value = list
    }

    fun isProductAdded(product: Product?): Boolean {
        val isAdded = list.any {
            val isMatch = it.itemName.split("(")[0].trim()+":"
            if (isMatch.equals(product?.productName+":")){
                it.itemName = "${product?.productName} ( ${product?.productPrice} )  $include ${it.quantity + 1}"
                it.quantity += 1
                it.totalPrice = ((product?.productPrice.toString()
                    .toDouble() * it.quantity) + product?.productTax.toString().toDouble())

                return@any true
            }
            false
        }
        _items.value = list
        return isAdded
    }

    fun clearOrder(){
        list.clear()
        _items.value = list
        amountValue.value = amountBuilder.clear().toString()
        updateDeselectCustomer()
        removeDiscount()
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
            total += it.totalPrice
        }

        return Config.CURRENCY+total.toString()
    }
    fun getSubTotalamountDouble(): Double {
        var total = 0.0
        list.forEach {
            total += it.totalPrice
        }

        return total
    }

    fun getTotalTax(): String {
        var total = 0.0
        list.forEach {
            total += it.taxRate
        }
        df.roundingMode = RoundingMode.DOWN
        return Config.CURRENCY+df.format(total).toString()
    }

    fun getTotalAmount() {
        var dis = 0
        var creditNoteAmountTemp = 0
        if (discounted != null){
            dis = discounted!!
        }else if (creditNoteAmount != null){
            creditNoteAmountTemp = creditNoteAmount!!
        }
        val totalTax = getTotalTax()
            .replace(Config.CURRENCY,"")
            .toDouble() + getSubTotalamount()
            .replace(Config.CURRENCY,"")
            .toDouble() - dis - creditNoteAmountTemp

        df.roundingMode = RoundingMode.DOWN
        _totalLivedata.value = Config.CURRENCY+df.format(totalTax)
    }

    fun getTotalAmountDouble(): Double {
        return  getTotalTax()
            .replace(Config.CURRENCY,"")
            .toDouble() + getSubTotalamount()
            .replace(Config.CURRENCY,"")
            .toDouble()
    }
    fun updateSelectedCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
        _isCustomerSelected.value = true
    }


    fun updateDeselectCustomer() {
        _selectedCustomer.value = null
        _isCustomerSelected.value = false
    }


    fun addDiscount(discount: String){
        discounted = discount.toInt()
        _isDiscountApplied.value = true
        getTotalAmount()

    }
    fun removeDiscount(){
        _isDiscountApplied.value = false
        discounted = null
        getTotalAmount()
    }

    fun addCreditNote(creditNote: CreditNote?){
        _selectedCreditNote.value = creditNote
        _isCreditNoteApplied.value = true

        creditNoteId = creditNote?.id
        creditNoteAmount = creditNote?.totalAmount?.toInt()

        getTotalAmount()

    }
    fun removeCreditNote(){
        _selectedCreditNote.value = null
        _isCreditNoteApplied.value = false

        creditNoteId = 0
        creditNoteAmount = 0

        getTotalAmount()
    }

    fun getCreditNote() : CreditNote? {
        return selectedCreditNote.value
    }

    fun getInvoice() : Invoice{
        val invoice = Invoice(
            invoiceId = generateInvoiceId(),
            invoiceNumber = "INV ${getInvoiceItemCount()}",
            invoiceDate = System.currentTimeMillis().toString(),
            invoiceTime = System.currentTimeMillis().toString(),
            createdBy = "Created By Admin",
            discount = discounted,
            totalItems = getInvoiceItem().size,
            subtotal = getSubTotalamountDouble(),
            cashAmount = getTotalAmountDouble(),
            totalAmount = (getTotalAmountDouble()- (discounted?:0) - (creditNoteAmount?:0)),
            customerId = getCustomerId(),
            isSynced = 0,
            creditNoteAmount = creditNoteAmount?:0,
            creditNoteId = creditNoteId?:0,
            status = "Active"
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

    fun generateInvoiceItemId(): Int {
        val timestamp = System.currentTimeMillis()
        val counter = (0 until 100).random() // Choose a random number as the counter
        return (timestamp / 100).toInt() * 100 + counter
    }
}
