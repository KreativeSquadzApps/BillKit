package com.kreativesquadz.billkit.ui.bills

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.GST
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.model.settings.PdfSettings
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import com.kreativesquadz.billkit.repository.GstTaxRepository
import com.kreativesquadz.billkit.repository.SettingsRepository
import com.kreativesquadz.billkit.repository.UserSettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiptViewModel @Inject constructor(val settingsRepository: SettingsRepository,
                                           val billHistoryRepository: BillHistoryRepository,
                                            val customerManagRepository: CustomerManagRepository,
                                           val taxRepository: GstTaxRepository,
                                           val userSettingRepository: UserSettingRepository

) : ViewModel() {
    lateinit var companyDetails : LiveData<Resource<CompanyDetails>>
    private val _invoiceItems = MutableLiveData<List<InvoiceItem>>()
    private val _gstValue = MutableLiveData<List<GST>>()
    val gstValue: LiveData<List<GST>> get() = _gstValue
    private val _pdfSettings = MutableStateFlow(PdfSettings())
    val pdfSettings: StateFlow<PdfSettings> = _pdfSettings.asStateFlow()


    val invoiceItems: LiveData<List<InvoiceItem>> get() = _invoiceItems



    fun getInvoiceDetails(invoiceId: String) : LiveData<Invoice> {
        return billHistoryRepository.getInvoiceById(invoiceId.toInt())
    }

    fun getCompanyDetailsRec(): LiveData<Resource<CompanyDetails>> {
        companyDetails = settingsRepository.loadCompanyDetails(Config.userId)
        return companyDetails
    }

    fun getCustomerById(id: String) : Customer {
        return customerManagRepository.getCustomer(id)
    }

    fun fetchInvoiceItems(invoiceId: Long) = viewModelScope.launch {
        try {
            val items = billHistoryRepository.getInvoiceItems(invoiceId)
            _invoiceItems.postValue(items)
        } catch (e: Exception) {
            // Handle exception, e.g., show a message to the user
        }
    }




    fun getGstListByValue(taxAmounts: List<Double>): LiveData<List<GST>> {
        return taxRepository.getGSTListByTaxValues(taxAmounts)
    }

    fun getPdfSetting()  {
        _pdfSettings.value = userSettingRepository.getPdfSetting(Config.userId) ?: PdfSettings()
    }

}