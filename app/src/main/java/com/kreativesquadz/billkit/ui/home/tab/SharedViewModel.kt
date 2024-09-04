package com.kreativesquadz.billkit.ui.home.tab

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.model.CreditNote
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.model.LoginResponse
import com.kreativesquadz.billkit.model.Product
import com.kreativesquadz.billkit.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.math.RoundingMode
import java.text.DecimalFormat
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(val loginRepository: LoginRepository) : ViewModel() {
    private val _items = MutableLiveData<MutableList<InvoiceItem>>().apply { value = mutableListOf() }
    val items: LiveData<MutableList<InvoiceItem>> get() = _items
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
    private var gstAddedAmount : Int? = null
    private var creditNoteAmount : Int? = null

    var _isDiscountApplied = MutableLiveData<Boolean>()
    val isDiscountApplied : LiveData<Boolean> get() = _isDiscountApplied

    var _isGstApplied = MutableLiveData<Boolean>()
    val isGstApplied : LiveData<Boolean> get() = _isGstApplied

    var _isCreditNoteApplied = MutableLiveData<Boolean>()
    val isCreditNoteApplied : LiveData<Boolean> get() = _isCreditNoteApplied


    var _totalLivedata = MutableLiveData<String>()
    val totalLivedata : LiveData<String> get() = _totalLivedata
    val df = DecimalFormat("#")
    var creditNoteId : Int?=0

   var isReversedAmountNQty = false

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
        val itemName = "Item${list.size + 1}"

        if (amount.contains("X")){
            val ammountArray =  amount.split("X")
            Log.e("ammountArray", ammountArray.toString())
            if (ammountArray.size == 2) {

                var (amnt, qty) = if (isReversedAmountNQty) {
                    ammountArray[1] to ammountArray[0]
                } else {
                    ammountArray[0] to ammountArray[1]
                }


                if (amnt.isEmpty()){
                    amnt = "1"
                }
                if (qty.isEmpty()){
                    qty = "1"
                }
                val finalAmount = amnt.replace("X", "").toDouble() * qty.toDouble()
                val invoiceId =  generateInvoiceId().toLong()
                val homeItem =  InvoiceItem(
                    invoiceId = invoiceId,
                    itemName = "$itemName ( $amnt )  $include $qty",
                    unitPrice = amnt.toDouble(),
                    quantity = qty.toInt(),
                    returnedQty = 0,
                    totalPrice = finalAmount,
                    taxRate = 0.00,
                    productMrp = amnt.toDouble()

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
                itemName = "$itemName ( $amountBuilder )  $include $qty",
                unitPrice = amountBuilder.toString().toDouble(),
                quantity = qty.toInt(),
                returnedQty = 0,
                totalPrice = finalAmount,
                taxRate = 0.00,
                productMrp = amountBuilder.toString().toDouble()

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
        var productMrp = product.productPrice
        if (product.productMrp != null && product.productMrp.toString().toDouble() > 0.0) {
            productMrp = product.productMrp
        }
        val homeItem =  InvoiceItem(
            invoiceId = invoiceId,
            itemName = "${product.productName}  ( ${product.productPrice} )   $include ${defaultQty}",
            unitPrice = product.productPrice.toString().toDouble(),
            quantity = defaultQty!!.toInt(),
            returnedQty = 0,
            totalPrice = ((product.productPrice.toString().toDouble() * defaultQty)),
            taxRate = product.productTax.toString().toDouble(),
            productMrp = productMrp

        )
        Log.e("productMrp", productMrp.toString())
        list.add(homeItem)
        _items.value = list
    }
    fun removeItemAt(position: Int){
        Log.e("pree",list.toString())
        list.removeAt(position)
        _items.value = list
        getSubTotalamount()

    }

    fun updateItemAt(oldItem: InvoiceItem, newItem : InvoiceItem){
        val position = list.indexOf(oldItem)
        Log.e("positionssss", position.toString())
        if (position != -1){
            list[position] = newItem
        }
        _items.value = list
    }

    fun isProductAdded(product: Product?): Boolean {
        val isAdded = list.any {
            val isMatch = it.itemName.split("(")[0].trim()+":"
            if (isMatch.equals(product?.productName+":")){
                it.itemName = "${product?.productName} ( ${product?.productPrice} )  $include ${it.quantity + 1}"
                it.quantity += 1
                it.totalPrice = ((product?.productPrice.toString()
                    .toDouble() * it.quantity))

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

    fun getTotalValues(): Triple<Double, Double, Double> {
        var subtotal = 0.0
        var totalTax = 0.0
        var totalAmount = 0.0

        list.forEach { item ->
            val itemTax = item.taxRate * item.unitPrice * item.quantity / 100
            totalTax += itemTax
            subtotal += item.totalPrice - itemTax
        }

        totalAmount = subtotal + totalTax - (discounted ?: 0) - (creditNoteAmount ?: 0)
        if (gstAddedAmount != null) {
            totalAmount += gstAddedAmount!!
        }

        return Triple(subtotal, totalTax, totalAmount)
    }

    fun getSubTotalamount(): String {
        val (subtotal, _, _) = getTotalValues()
        return Config.CURRENCY + subtotal.toString()
    }

    fun getTotalTax(): String {
        val (_, totalTax, _) = getTotalValues()
        df.roundingMode = RoundingMode.DOWN
        return Config.CURRENCY + df.format(totalTax)
    }

    fun getTotalAmount() {
        val (_, _, totalAmount) = getTotalValues()
        df.roundingMode = RoundingMode.DOWN
        _totalLivedata.value = Config.CURRENCY + df.format(totalAmount)
    }

    fun getTotalAmountDouble(): Double {
        val (_, _, totalAmount) = getTotalValues()
        return totalAmount
    }

    fun getSubTotalamountDouble(): Double {
        val (subtotal, _, _) = getTotalValues()
        return subtotal
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

    fun addGst(gst: String){
        gstAddedAmount = gst.toInt()
        _isGstApplied.value = true
        getTotalAmount()

    }
    fun removeGst(){
        _isGstApplied.value = false
        gstAddedAmount = null
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

    fun getInvoice( onlineAmount: Double?, creditAmount: Double?, cashAmount: Double? ) : Invoice{
        var createdBy = "Created By Admin"
        val loginSession = loginRepository.getUserSessions()
        if (loginSession != null){
            if (loginSession.staffId != null){
              val loginResponse = loginRepository.getSession(null, loginSession.staffId.toLong())
                val staff = loginResponse?.staff
                if (staff != null){
                    createdBy = "Created By ${staff.name}"
                }
            }
        }
        val invoice = Invoice(
            invoiceId = generateInvoiceId(),
            invoiceNumber = "INV ${getInvoiceItemCount()}",
            invoiceDate = System.currentTimeMillis().toString(),
            invoiceTime = System.currentTimeMillis().toString(),
            createdBy = createdBy,
            discount = discounted,
            totalItems = getInvoiceItem().size,
            subtotal = getSubTotalamountDouble(),
            cashAmount = cashAmount,
            onlineAmount = onlineAmount,
            creditAmount = creditAmount,
            totalAmount = getTotalAmountDouble(),
            totalGst = gstAddedAmount?.toDouble() ?: 0.0,
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

    fun setItemsList(items: List<InvoiceItem>) {
        list = items.toMutableList()
        _items.value = list
    }
    fun clearItemsList() {
        viewModelScope.launch {
            delay(300)
            list.clear()
            _items.value = list
        }
    }

    fun isSavedOrderIdExist(): Long? {
        list.forEach {
            if (it.orderId != 0L){
                return it.orderId
            }
        }
        return null
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
