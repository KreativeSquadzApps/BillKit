package com.kreativesquadz.hisabkitab.ui.bills.billHistory.saleReturn

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
import com.kreativesquadz.hisabkitab.model.Invoice
import com.kreativesquadz.hisabkitab.model.InvoiceItem
import com.kreativesquadz.hisabkitab.repository.BillHistoryRepository
import com.kreativesquadz.hisabkitab.repository.CreditNoteRepository
import com.kreativesquadz.hisabkitab.repository.InventoryRepository
import com.kreativesquadz.hisabkitab.worker.SyncCreditNoteWorker
import com.kreativesquadz.hisabkitab.worker.UpdateInvoiceStatusWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SaleReturnViewModel @Inject constructor(val inventoryRepository: InventoryRepository,
                                              val billHistoryRepository: BillHistoryRepository,
                                              val creditNoteRepository: CreditNoteRepository) : ViewModel() {

    private val _invoiceItems = MutableLiveData<List<InvoiceItem>>()
    val invoiceItems: LiveData<List<InvoiceItem>> get() = _invoiceItems

    private val _invoice = MutableLiveData<Invoice>()
    val invoice: LiveData<Invoice> get() = _invoice

    fun generateCreditNote(
        context: Context,
        creditNote: CreditNote,
        isRefund: Boolean,
        isSuccess: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (!isRefund) {
                    val existingNote = creditNoteRepository.getCreditNoteByInvoiceId(creditNote.invoiceId)
                    if (existingNote != null) {
                        creditNoteRepository.updateCreditNote(creditNote)
                    } else {
                        creditNoteRepository.addCreditNote(creditNote)
                    }
                    scheduleCreditNoteSync(context)
                }

                creditNote.invoiceItems.forEach {
                    billHistoryRepository.updateInvoiceItem(it.invoiceId, it.itemName, it.returnedQty!!)
                }

                creditNoteRepository.updateInvoiceStatus(creditNote.invoiceId.toInt())
                updateInvoiceStatusWork(context, "Returned", creditNote.invoiceId.toString())

                // Indicate success
                isSuccess(true)
            } catch (e: Exception) {
                // Log the exception if needed
                e.printStackTrace()
                // Indicate failure
                isSuccess(false)
            }
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


    fun getInvoiceDetails(invoiceId: String) {
        viewModelScope.launch {
            val invoiceData = billHistoryRepository.getInvoiceByIdWithoutLiveData(invoiceId.toInt())
            _invoice.postValue(invoiceData)
        }
    }


    private fun scheduleCreditNoteSync(context: Context) {
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
    private fun updateInvoiceStatusWork (context: Context, status: String, invoiceId: String ) {
        val data = Data.Builder()
            .putString("invoiceId",invoiceId)
            .putString("status", status)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<UpdateInvoiceStatusWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "updateInvoiceStatusWorker",
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )
    }

}