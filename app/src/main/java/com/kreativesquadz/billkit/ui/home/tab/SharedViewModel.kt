package com.kreativesquadz.billkit.ui.home.tab

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.CreditNote
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.model.LoginResponse
import com.kreativesquadz.billkit.model.Product
import com.kreativesquadz.billkit.model.settings.GST
import com.kreativesquadz.billkit.model.settings.TaxOption
import com.kreativesquadz.billkit.model.settings.TaxSettings
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.GstTaxRepository
import com.kreativesquadz.billkit.repository.LoginRepository
import com.kreativesquadz.billkit.repository.SettingsRepository
import com.kreativesquadz.billkit.utils.TaxType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.DecimalFormat
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(val workManager: WorkManager,
                                          val loginRepository: LoginRepository,
                                          val settingsRepository: SettingsRepository,
                                          val gstTaxRepository: GstTaxRepository,
                                          val billHistoryRepository: BillHistoryRepository) : ViewModel() {

    private var invoicePrefix: String? = null
     private var invoiceNumber: Int? = null
    private val _items = MutableLiveData<MutableList<InvoiceItem>>().apply { value = mutableListOf() }
    val items: LiveData<MutableList<InvoiceItem>> get() = _items
    var list = mutableListOf<InvoiceItem>()
    private val _selectedCustomer = MutableLiveData<Customer?>()
    val selectedCustomer: LiveData<Customer?> get() = _selectedCustomer
    val _invoiceItems = MutableLiveData<List<InvoiceItem>>()
    val invoiceItems: LiveData<List<InvoiceItem>> get() = _invoiceItems
    public val _selectedCreditNote = MutableLiveData<CreditNote?>()
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
    private var packageAmount : Int? = null
    private var creditNoteAmount : Int? = null

    var _isDiscountApplied = MutableLiveData<Boolean>()
    val isDiscountApplied : LiveData<Boolean> get() = _isDiscountApplied

    var _isGstApplied = MutableLiveData<Boolean>()
    val isGstApplied : LiveData<Boolean> get() = _isGstApplied

    var _isPackageApplied = MutableLiveData<Boolean>()
    val isPackageApplied : LiveData<Boolean> get() = _isPackageApplied

    var _isCreditNoteApplied = MutableLiveData<Boolean>()
    val isCreditNoteApplied : LiveData<Boolean> get() = _isCreditNoteApplied


    var _totalLivedata = MutableLiveData<String>()
    val totalLivedata : LiveData<String> get() = _totalLivedata
    val df = DecimalFormat("#")
    var creditNoteId : Int?=0
    var isReversedAmountNQty = false
    val taxSettings: LiveData<TaxSettings> = gstTaxRepository.getTaxSettings()
    var invoiceId =  generateInvoiceId().toLong()


    fun initializeTaxSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            val taxSettings = gstTaxRepository.getTaxSettingsObj()
            if (taxSettings == null) {
                // Insert default tax settings if no settings exist
                val defaultTaxSettings = TaxSettings(
                    defaultTaxOption = TaxOption.ExemptTax, // Default value
                    selectedTaxPercentage = 0.0f // Default percentage
                )
                gstTaxRepository.saveTaxSettings(defaultTaxSettings)
            }
        }

    }


    // Function to update the default tax setting when user changes it in settings


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


    fun addItem(taxRate: Float?){

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
                val itemAmount = amnt.replace("X", "").toDouble()
               val selectedTaxPercentage = taxSettings.value?.selectedTaxPercentage

                var finalPrice = finalAmount

                taxSettings.value?.defaultTaxOption?.let {
                    if (it == TaxOption.ExemptTax){
                        finalPrice = finalAmount
                    }
                    if (it == TaxOption.PriceIncludesTax){
                        finalPrice = finalAmount
                    }
                    if (it == TaxOption.PriceExcludesTax){
                        selectedTaxPercentage?.let {
                            val productTax =   itemAmount.times(it).div(100)
                            finalPrice = finalAmount   +  (productTax * qty.toDouble())
                        }
                    }
                    if (it == TaxOption.ZeroRatedTax){
                        finalPrice = finalAmount
                    }

                }
                val homeItem =  InvoiceItem(
                    id = 0,
                    orderId = invoiceId,
                    invoiceId = invoiceId,
                    itemName = "$itemName ( $amnt ) X $qty",
                    unitPrice = amnt.toDouble(),
                    quantity = qty.toInt(),
                    returnedQty = 0,
                    totalPrice = finalPrice,
                    taxRate = taxRate?.toDouble() ?: 0.00,
                    productMrp = amnt.toDouble()
                )

                list.add(homeItem)
            }
        }else{
            val include = "X"
            val qty = "1"
            val finalAmount = amount.toDouble() * qty.toDouble()
            val selectedTaxPercentage = taxSettings.value?.selectedTaxPercentage

            var finalPrice = finalAmount

            taxSettings.value?.defaultTaxOption?.let {
                if (it == TaxOption.ExemptTax){
                    finalPrice = finalAmount
                }
                if (it == TaxOption.PriceIncludesTax){
                    finalPrice = finalAmount
                }
                if (it == TaxOption.PriceExcludesTax){
                    selectedTaxPercentage?.let {
                        val productTax =   finalAmount.times(it).div(100)
                        finalPrice = finalAmount   +  (productTax * qty.toDouble())
                    }
                }
                if (it == TaxOption.ZeroRatedTax){
                    finalPrice = finalAmount
                }

            }
            val homeItem =  InvoiceItem(
                orderId = invoiceId,
                invoiceId = invoiceId,
                itemName = "$itemName ( $amountBuilder )  $include $qty",
                unitPrice = amountBuilder.toString().toDouble(),
                quantity = qty.toInt(),
                returnedQty = 0,
                totalPrice = finalPrice,
                taxRate = taxRate?.toDouble() ?: 0.00,
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
        val defaultQty = product.productDefaultQty ?: 1
        var productMrp = product.productPrice
        if (product.productMrp != null && product.productMrp.toString().toDouble() > 0.0) {
            productMrp = product.productMrp
        }
        val finalTotalPrice = (product.productPrice.toString().toDouble() * defaultQty)
         var finalPrice = finalTotalPrice

            product.productTaxType?.let { taxTypeString ->
            val taxType = TaxType.fromString(taxTypeString)
            taxType?.let { type ->
                when (type) {
                    TaxType.PriceIncludesTax -> {
                        product.productTax?.let { it1 ->
                            val productTax =   product.productPrice?.times(it1)?.div(100) ?: 0.0
                            finalPrice = finalTotalPrice
                        }

                        // Handle PriceIncludesTax
                    }
                    TaxType.PriceWithoutTax -> {
                        product.productTax?.let { it1 ->
                            val productTax =   product.productPrice?.times(it1)?.div(100) ?: 0.0
                            finalPrice = finalTotalPrice   +  (productTax * defaultQty)
                        }
                    // Handle PriceWithoutTax
                    }
                    TaxType.ZeroRatedTax -> {

                        // Handle ZeroRatedTax
                    }
                    TaxType.ExemptTax -> {
                        // Handle ExemptTax
                    }
                }
            }
        }

        val homeItem =  InvoiceItem(
            orderId = invoiceId,
            invoiceId = invoiceId,
            itemName = "${product.productName}  ( ${product.productPrice} )   $include ${defaultQty}",
            unitPrice = product.productPrice.toString().toDouble(),
            quantity = defaultQty,
            returnedQty = 0,
            totalPrice = finalPrice,
            taxRate = product.productTax?.toString()?.toDouble() ?: 0.0,
            productMrp = productMrp,
            )
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
        _invoiceItems.value = list
        _items.value = list
    }

    fun fetchInvoiceItems(id: Long) = viewModelScope.launch {
        if (_invoiceItems.value.isNullOrEmpty()) { // Only fetch if data is not already loaded
            try {
                val invoiceItemsDeferred = async { billHistoryRepository.getInvoiceItems(id)}
                _invoiceItems.value = invoiceItemsDeferred.await()
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }
    fun isProductAdded(product: Product?): Boolean {
        val isAdded = list.any {
            val isMatch = it.itemName.split("(")[0].trim()
            if (isMatch == product?.productName?.trim()){
                it.itemName = "${product.productName} ( ${product.productPrice} )  $include ${it.quantity + 1}"
                if (product.productDefaultQty !=null && product.productDefaultQty > 0){
                    it.quantity += product.productDefaultQty
                }else{
                    it.quantity += 1
                }


                val finalTotalPrice = (product.productPrice.toString().toDouble() * it.quantity)
                var finalPrice = finalTotalPrice

                product.productTaxType?.let { taxTypeString ->
                    val taxType = TaxType.fromString(taxTypeString)
                    taxType?.let { type ->
                        when (type) {
                            TaxType.PriceIncludesTax -> {
                                product.productTax?.let { it1 ->
                                val productTax =   product.productPrice?.times(it1)?.div(100) ?: 0.0
                                    finalPrice = finalTotalPrice   +  (productTax * it.quantity)
                                }
                                // Handle PriceIncludesTax
                            }
                            TaxType.PriceWithoutTax -> {
                                product.productTax?.let { it1 ->
                                    val productTax =   product.productPrice?.times(it1)?.div(100) ?: 0.0
                                    finalPrice = finalTotalPrice   +  (productTax * it.quantity)
                                }
                            }
                            TaxType.ZeroRatedTax -> {
                                // Handle ZeroRatedTax
                            }
                            TaxType.ExemptTax -> {
                                // Handle ExemptTax
                            }
                        }
                    }
                }

                it.totalPrice = finalPrice

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
        removeGst()
        removePackage()
        removeCreditNote()
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
            totalTax = item.taxRate * item.unitPrice * item.quantity / 100
            subtotal += item.totalPrice - totalTax
            totalAmount += item.totalPrice
        }

        totalAmount = totalAmount - (discounted ?: 0) - (creditNoteAmount ?: 0)
        if (gstAddedAmount != null) {
            totalAmount += gstAddedAmount!!
        }
        if (packageAmount != null) {
            totalAmount += packageAmount!!
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

    fun addPackage(packageAmount: String){
        this.packageAmount = packageAmount.toDouble().toInt()
        _isPackageApplied.value = true
        getTotalAmount()
    }
    fun removePackage(){
        _isPackageApplied.value = false
        this.packageAmount = null
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

    fun getInvoice( onlineAmount: Double?, creditAmount: Double?, cashAmount: Double?,customGstAmount: String?,invoicePrefixNumber : String) : Invoice{
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
            invoiceId = invoiceId.toInt(),
            invoiceNumber = invoicePrefixNumber,
            invoiceDate = System.currentTimeMillis().toString(),
            invoiceTime = System.currentTimeMillis().toString(),
            createdBy = createdBy,
            discount = discounted,
            totalItems = getInvoiceItem().size,
            subtotal = getSubTotalamountDouble(),
            cashAmount = cashAmount,
            onlineAmount = onlineAmount,
            creditAmount = creditAmount,
            packageAmount = packageAmount?.toDouble(),
            customGstAmount = customGstAmount,
            totalAmount = getTotalAmountDouble(),
            totalGst = gstAddedAmount?.toDouble() ?: 0.0,
            customerId = getCustomerId(),
            isSynced = 0,
            creditNoteAmount = creditNoteAmount?:0,
            creditNoteId = creditNoteId?:0,
            status = "Active",
            invoiceItems =  getInvoiceItem()
        )
        viewModelScope.launch {
            invoiceNumber?.let {
                settingsRepository.updateInvoiceNumber(Config.userId, it.plus(1))
            }
        }

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

   public fun generateInvoiceId(): Int {
        // Generate a unique invoiceId using a combination of timestamp and counter
        val timestamp = System.currentTimeMillis()
        val counter = (0 until 1000).random() // Choose a random number as the counter
        return (timestamp / 1000).toInt() * 1000 + counter
    }

     fun loadCompanyDetails() : LiveData<Resource<CompanyDetails>> {
        return  settingsRepository.loadCompanyDetails(Config.userId)
    }

    fun loadCompanyDetailsDb() : LiveData<CompanyDetails> {
        return  settingsRepository.loadCompanyDetailsDb(Config.userId)
    }

}
