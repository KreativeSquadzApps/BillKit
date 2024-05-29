package com.kreativesquadz.billkit.ui.home.billDetails

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kreativesquadz.billkit.api.ApiStatus
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.worker.SyncInvoicesWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class BillDetailsViewModel @Inject constructor(val billHistoryRepository: BillHistoryRepository)
    : ViewModel() {

    private val _invoiceStatus = MutableLiveData<Boolean?>()
    val invoiceApiStatus: LiveData<Boolean?> get() = _invoiceStatus

    private val _invoiceID = MutableLiveData<Long?>()
    val invoiceId: LiveData<Long?> get() = _invoiceID

   var invoiceIdLong : Long?=null

    fun addInvoice(invoice: Invoice,context: Context) {
        viewModelScope.launch {
            _invoiceID.value = billHistoryRepository.addInvoice(invoice)
            scheduleInvoiceSync(context)
            _invoiceStatus.value = true
        }

    }

    fun getInvoiceById(id: Int): LiveData<Invoice> {
        return billHistoryRepository.getInvoiceById(id)
    }


    fun scheduleInvoiceSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncInvoicesWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "invoiceSyncWork",
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )
    }


}