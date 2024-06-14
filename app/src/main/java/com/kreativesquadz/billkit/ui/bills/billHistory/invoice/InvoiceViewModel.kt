package com.kreativesquadz.billkit.ui.bills.billHistory.invoice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(val customerManagRepository: CustomerManagRepository,val billHistoryRepository: BillHistoryRepository) : ViewModel() {
  private val _invoiceItems = MutableLiveData<List<InvoiceItem>>()
  val invoiceItems: LiveData<List<InvoiceItem>> get() = _invoiceItems
  fun getCustomerById(id: String) : Customer {
    return customerManagRepository.getCustomer(id)
  }
  fun getInvoiceDetails(invoiceId: String) : LiveData<Invoice> {
    return billHistoryRepository.getInvoiceById(invoiceId.toInt())
  }
  fun fetchInvoiceItems(id: Long) = viewModelScope.launch {
    try {
      val items = billHistoryRepository.getInvoiceItems(id)
      _invoiceItems.postValue(items)
    } catch (e: Exception) {
      // Handle exception, e.g., show a message to the user
    }
  }
}