package com.kreativesquadz.billkit.ui.bills.creditNote.creditNoteDetails

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.CreditNote
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CreditNoteRepository
import com.kreativesquadz.billkit.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreditNoteDetailsViewModel @Inject constructor(val creditNoteRepository: CreditNoteRepository, val billHistoryRepository: BillHistoryRepository) : ViewModel() {
    var _creditNote = MutableLiveData<CreditNote>()
    val creditNote : LiveData<CreditNote> get() = _creditNote

    var _itemsList = MutableLiveData<List<InvoiceItem>>()
    val itemsList : LiveData<List<InvoiceItem>> get() = _itemsList

    fun getCreditNote(invoiceId:Long){
        viewModelScope.launch {
            _itemsList.value =  billHistoryRepository.getInvoiceItems(invoiceId)
            _creditNote.value = creditNoteRepository.getCreditNoteByInvoiceId(invoiceId)
        }
    }

}