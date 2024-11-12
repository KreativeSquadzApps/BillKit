package com.kreativesquadz.billkit.ui.bills.billHistory.invoice

import android.content.Context
import android.util.Log
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
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import com.kreativesquadz.billkit.worker.SyncCustomerWorker
import com.kreativesquadz.billkit.worker.UpdateInvoiceStatusWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(val customerManagRepository: CustomerManagRepository,
                                           val billHistoryRepository: BillHistoryRepository,
                                                                                     ) : ViewModel() {
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
                Log.e("InvoiceViewModel", "Fetched invoice items: $items")
                _invoiceItems.postValue(items)
            } catch (e: Exception) {
                // Handle exception
            }

    }
   fun updateInvoiceStatus( context: Context, status: String, invoiceId: Int) {
     viewModelScope.launch {
       billHistoryRepository.updateInvoiceStatus(status,invoiceId)
           updateInvoiceStatusWork(context,status,invoiceId.toString())
        }
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