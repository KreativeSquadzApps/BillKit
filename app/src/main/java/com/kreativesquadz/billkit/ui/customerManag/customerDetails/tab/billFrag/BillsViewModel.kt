package com.kreativesquadz.billkit.ui.customerManag.customerDetails.tab.billFrag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillsViewModel  @Inject constructor(val repository: BillHistoryRepository) : ViewModel() {

    private val _invoices = MutableLiveData<List<Invoice>>()
    val invoices: LiveData<List<Invoice>> = _invoices

    fun getAllInvoicesCustomer(customerId: Long) {
        viewModelScope.launch {
            repository.getAllInvoicesFlow(customerId).collect { invoiceList ->
                _invoices.postValue(invoiceList)
            }
        }
    }
}