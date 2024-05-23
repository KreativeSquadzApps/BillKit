package com.kreativesquadz.billkit.ui.home.billDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class BillDetailsViewModel @Inject constructor(val billHistoryRepository: BillHistoryRepository
) : ViewModel() {
   private val _invoiceStatus = MutableLiveData<Boolean>()
    val invoiceStatus: LiveData<Boolean> get() = _invoiceStatus

    fun generateInvoice(invoice: Invoice) {
        viewModelScope.launch {
            val result = billHistoryRepository.generateInvoice(invoice)
            _invoiceStatus.value = result
        }
    }


}