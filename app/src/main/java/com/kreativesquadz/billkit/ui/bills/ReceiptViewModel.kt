package com.kreativesquadz.billkit.ui.bills

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import com.kreativesquadz.billkit.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReceiptViewModel @Inject constructor(val settingsRepository: SettingsRepository,
                                           val billHistoryRepository: BillHistoryRepository
) : ViewModel() {

    fun getInvoiceDetails(invoiceId: String) : LiveData<Invoice> {
        return billHistoryRepository.getInvoiceById(invoiceId.toInt())
    }

    fun getCompanyDetails() : LiveData<Resource<List<CompanyDetails>>>  {
       return settingsRepository.loadCompanyDetails()
    }






}