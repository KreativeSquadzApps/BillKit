package com.kreativesquadz.billkit.ui.bills.billHistory.searchBill

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchBillViewModel @Inject constructor(var repository: BillHistoryRepository) : ViewModel() {

    private var _invoice = MutableLiveData<Invoice>()
    val invoice: LiveData<Invoice> = _invoice

    fun searchBill(invoiceNumber: String) {
        _invoice.value = repository.getInvoiceByInvoiceNumberWithoutLiveData(invoiceNumber)
    }
}