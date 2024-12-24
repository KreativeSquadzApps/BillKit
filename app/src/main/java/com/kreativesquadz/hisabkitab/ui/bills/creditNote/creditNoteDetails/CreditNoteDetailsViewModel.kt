package com.kreativesquadz.hisabkitab.ui.bills.creditNote.creditNoteDetails

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kreativesquadz.hisabkitab.model.CreditNote
import com.kreativesquadz.hisabkitab.model.InvoiceItem
import com.kreativesquadz.hisabkitab.repository.BillHistoryRepository
import com.kreativesquadz.hisabkitab.repository.CreditNoteRepository
import com.kreativesquadz.hisabkitab.worker.UpdateCreditNoteWorker
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

    fun updateCreditNote(context: Context, invoiceId:Long,creditNote: CreditNote){
        viewModelScope.launch {
            creditNoteRepository.updateCreditNote(creditNote)
            _creditNote.value = creditNoteRepository.getCreditNoteByInvoiceId(invoiceId)
        }
        updateCreditNoteStatusWork(context, creditNote.status,creditNote.id.toString())
    }

    private fun updateCreditNoteStatusWork (context: Context, status: String, id: String ) {
        val data = Data.Builder()
            .putString("id",id)
            .putString("status", status)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<UpdateCreditNoteWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "updateCreditNoteStatusWorker",
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )
    }
}