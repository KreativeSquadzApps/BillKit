package com.kreativesquadz.hisabkitab.ui.bills.creditNote.creditNoteDetails.creditNoteReceipt

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.model.CompanyDetails
import com.kreativesquadz.hisabkitab.model.Customer
import com.kreativesquadz.hisabkitab.model.InvoiceItem
import com.kreativesquadz.hisabkitab.model.settings.InvoicePrinterSettings
import com.kreativesquadz.hisabkitab.model.settings.PdfSettings
import com.kreativesquadz.hisabkitab.model.settings.TaxSettings
import com.kreativesquadz.hisabkitab.model.settings.ThermalPrinterSetup
import com.kreativesquadz.hisabkitab.repository.BillHistoryRepository
import com.kreativesquadz.hisabkitab.repository.BluetoothRepository
import com.kreativesquadz.hisabkitab.repository.CustomerManagRepository
import com.kreativesquadz.hisabkitab.repository.GstTaxRepository
import com.kreativesquadz.hisabkitab.repository.SettingsRepository
import com.kreativesquadz.hisabkitab.repository.UserSettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreditNoteReceiptViewModel @Inject constructor(val settingsRepository: SettingsRepository,
                                                     val billHistoryRepository: BillHistoryRepository,
                                                     val customerManagRepository: CustomerManagRepository,
                                                     val taxRepository: GstTaxRepository,
                                                     val userSettingRepository: UserSettingRepository,
                                                     private val repository: BluetoothRepository
) : ViewModel() {


    val printStatus = MutableLiveData<String>()

    private val _printerSettings = MutableStateFlow(ThermalPrinterSetup())
    val printerSettings: StateFlow<ThermalPrinterSetup> = _printerSettings.asStateFlow()

    private val _companyDetails = MutableLiveData<CompanyDetails>()
    val companyDetails: LiveData<CompanyDetails> = _companyDetails


    val _invoiceItems = MutableLiveData<List<InvoiceItem>>()
    val invoiceItems: LiveData<List<InvoiceItem>> get() = _invoiceItems

    val _taxSettings = MutableLiveData<TaxSettings>()
    val taxSettings: LiveData<TaxSettings> = _taxSettings


    private val _pdfSettings = MutableStateFlow(PdfSettings())
    val pdfSettings: StateFlow<PdfSettings> = _pdfSettings.asStateFlow()


    private val _invoicePrinterSettings = MutableStateFlow(InvoicePrinterSettings())
    val invoicePrinterSettings: StateFlow<InvoicePrinterSettings> = _invoicePrinterSettings.asStateFlow()


    private val _allDataReady = MutableLiveData<Boolean>()
    val allDataReady: LiveData<Boolean> = _allDataReady


    fun fetchAllDetails(invoiceId: String) = viewModelScope.launch {
        try {
            val companyDetailsDeferred = async { settingsRepository.loadCompanyDetailsDb(Config.userId).asFlow().first()}
            val invoiceItemsDeferred = async { billHistoryRepository.getInvoiceItems(invoiceId.toLong())}
            val taxSettingsDeferred = async { taxRepository.getTaxSettings().asFlow().first()}
            val pdfSettingsDeferred = async { userSettingRepository.getPdfSetting(Config.userId) ?: PdfSettings()}
            val invoicePrinterSettingsDeferred = async { userSettingRepository.getInvoicePrinterSetting(
                Config.userId) ?: InvoicePrinterSettings()}


            val companyDetails = companyDetailsDeferred.await()
            val invoiceItems = invoiceItemsDeferred.await()
            val taxSettings = taxSettingsDeferred.await()
            val pdfSettings = pdfSettingsDeferred.await()
            val invoicePrinterSettings = invoicePrinterSettingsDeferred.await()

            _companyDetails.value = companyDetails
            _invoiceItems.value = invoiceItems
            _taxSettings.value = taxSettings
            _pdfSettings.value = pdfSettings
            _invoicePrinterSettings.value = invoicePrinterSettings

            checkIfAllDataReady()
        } catch (e: Exception) {
            // Handle exceptions
        }
    }
    private fun checkIfAllDataReady() {
        if (_companyDetails.value != null && _invoiceItems.value != null && _taxSettings.value != null) {
            _allDataReady.value = true
        }
    }

    fun getCustomerById(id: String) : Customer {
        return customerManagRepository.getCustomer(id)
    }

    private fun insertPrinterSetting(thermalPrinterSetup: ThermalPrinterSetup) {
        viewModelScope.launch {
            userSettingRepository.insertPrinterSetting(thermalPrinterSetup)
            _printerSettings.value = userSettingRepository.getPrinterSetting(Config.userId)  ?: ThermalPrinterSetup()
        }
    }

    fun getPrinterSetting()  {
        if (userSettingRepository.getPrinterSetting(Config.userId) == null){
            insertPrinterSetting(ThermalPrinterSetup(userId = Config.userId))
        }
        else{
            _printerSettings.value = userSettingRepository.getPrinterSetting(Config.userId) ?: ThermalPrinterSetup()
        }
    }

    fun printUsingDefaultPrinter(data: ByteArray){
        printStatus.postValue("printingStart")
        viewModelScope.launch {
            try {
                val printer = userSettingRepository.getPrinterSetting(Config.userId)
                if (printer != null){
                    if (printer.defaultPrinterAddress.isNotEmpty()) {
                        val result = repository.connectToDevice(printer.defaultPrinterAddress)
                        if (result) {
                            repository.printData(data)
                            printStatus.postValue("Print Successful")
                        } else {
                            printStatus.postValue("Failed to connect to default printer")
                        }
                    }else{
                        printStatus.postValue("No default printer found")
                    }
                }

            } catch (e: SecurityException) {
                printStatus.postValue(e.message)

            }
        }
    }

}