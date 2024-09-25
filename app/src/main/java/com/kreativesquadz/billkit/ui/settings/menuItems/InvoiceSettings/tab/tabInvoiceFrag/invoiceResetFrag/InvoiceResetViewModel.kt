package com.kreativesquadz.billkit.ui.settings.menuItems.InvoiceSettings.tab.tabInvoiceFrag.invoiceResetFrag

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
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.InvoicePrefixNumber
import com.kreativesquadz.billkit.repository.SettingsRepository
import com.kreativesquadz.billkit.worker.AddInvoicePrefixWorker
import com.kreativesquadz.billkit.worker.UpdateInvoicePrefixWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceResetViewModel  @Inject constructor(val workManager: WorkManager,
                                                 val settingsRepository: SettingsRepository ) : ViewModel() {

//    private val _invoicePrefix = MutableLiveData<String>()
//    val invoicePrefix: LiveData<String> get() = _invoicePrefix

    private val _invoicePrefixNumberList = MutableLiveData<Resource<List<InvoicePrefixNumber>>>()
    var invoicePrefixNumberList: LiveData<Resource<List<InvoicePrefixNumber>>> = _invoicePrefixNumberList

    fun getInvoicePrefixNumberList() {
        // Trigger the loading of data and update the LiveData object
        invoicePrefixNumberList = settingsRepository.loadInvoicePrefixNumberList(Config.userId)
        Log.d("InvoiceResetViewModel", "getInvoicePrefixNumberList: ${invoicePrefixNumberList.value}")
    }

    // StateFlow for loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun addInvoicePrefixNumber(invoicePrefixNumber: InvoicePrefixNumber){
        viewModelScope.launch(Dispatchers.IO) {
           val id = settingsRepository.insertInvoicePrefixNumber(invoicePrefixNumber)
            addInvoicePrefixNumberWorker(id)
        }
    }

    private fun addInvoicePrefixNumberWorker(id: Long) {
        val data = Data.Builder()
            .putLong("id", id)
            .build()
        Log.d("InvoiceResetViewModel", "addInvoicePrefixNumberWorker: $data")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequestkkk = OneTimeWorkRequestBuilder<AddInvoicePrefixWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        workManager.enqueueUniqueWork(
            "InvoicePrefixNumberWorker",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequestkkk
        )
    }

    private fun updateInvoicePrefixNumberWorker(id: Long) {
        val data = Data.Builder()
            .putLong("id", id)
            .build()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequestsss = OneTimeWorkRequestBuilder<UpdateInvoicePrefixWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        workManager.enqueueUniqueWork(
            "updatedInvoicePrefixNumberWorker",
            ExistingWorkPolicy.APPEND,
            syncWorkRequestsss
        )
    }

    fun reuseInvoicePrefixNumber(invoicePrefixNumber: InvoicePrefixNumber) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                settingsRepository.updateInvoiceNumberAndPrefix(Config.userId,
                    invoicePrefixNumber.id.toLong() ,
                    invoicePrefixNumber.invoicePrefix,
                    invoicePrefixNumber.invoiceNumber.toInt())
                getInvoicePrefixNumberList()
                updateInvoicePrefixNumberWorker(invoicePrefixNumber.id.toLong())
            } catch (e: Exception) {
                // Handle error or log it
                Log.e("ViewModel", "Error: ${e.message}")
            }
        }
    }

    fun deleteInvoicePrefixNumber(id:Long){
        viewModelScope.launch {
            settingsRepository.deleteInvoicePrefixNumber(id)
        }
    }

}