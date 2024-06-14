package com.kreativesquadz.billkit.ui.bills.billHistory.saleReturn

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kreativesquadz.billkit.model.CreditNote
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CreditNoteRepository
import com.kreativesquadz.billkit.repository.InventoryRepository
import com.kreativesquadz.billkit.worker.SyncCreditNoteWorker
import com.kreativesquadz.billkit.worker.SyncCustomerWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SaleReturnViewModel @Inject constructor(val inventoryRepository: InventoryRepository,
                                              val billHistoryRepository: BillHistoryRepository,
                                              val creditNoteRepository: CreditNoteRepository) : ViewModel() {

    private val _invoiceItems = MutableLiveData<List<InvoiceItem>>()
    val invoiceItems: LiveData<List<InvoiceItem>> get() = _invoiceItems

    fun generateCreditNote(context: Context,creditNote: CreditNote){
        viewModelScope.launch {
            val existingNote = creditNoteRepository.getCreditNoteByInvoiceId(creditNote)
            Log.e("existingNote",existingNote.toString())
            Log.e("creditNotessssss",creditNote.toString())
            if (existingNote != null) {
                creditNoteRepository.updateCreditNote(creditNote)
            } else {
                creditNoteRepository.addCreditNote(creditNote)
            }
            creditNoteRepository.updateInvoiceStatus(creditNote.invoiceId.toInt())
            creditNote.invoiceItems.forEach{
                billHistoryRepository.updateInvoiceItem(it.invoiceId,it.itemName,it.returnedQty!!)
            }
            scheduleCreditNoteSync(context)
        }
    }

    fun fetchInvoiceItems(invoiceId: Long) = viewModelScope.launch {
        try {
            val items = billHistoryRepository.getInvoiceItems(invoiceId)
            _invoiceItems.postValue(items)
        } catch (e: Exception) {
            // Handle exception, e.g., show a message to the user
        }
    }


    fun getInvoiceDetails(invoiceId: String) : LiveData<Invoice> {
        return billHistoryRepository.getInvoiceById(invoiceId.toInt())
    }


    fun scheduleCreditNoteSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncCreditNoteWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "creditNoteSyncWork",
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )
    }


}