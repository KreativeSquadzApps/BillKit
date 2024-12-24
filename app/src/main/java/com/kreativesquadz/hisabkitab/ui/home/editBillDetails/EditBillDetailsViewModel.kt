package com.kreativesquadz.hisabkitab.ui.home.editBillDetails

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.model.CreditNote
import com.kreativesquadz.hisabkitab.model.Customer
import com.kreativesquadz.hisabkitab.model.Invoice
import com.kreativesquadz.hisabkitab.model.InvoiceItem
import com.kreativesquadz.hisabkitab.model.settings.UserSetting
import com.kreativesquadz.hisabkitab.repository.BillHistoryRepository
import com.kreativesquadz.hisabkitab.repository.CreditNoteRepository
import com.kreativesquadz.hisabkitab.repository.CustomerManagRepository
import com.kreativesquadz.hisabkitab.repository.InventoryRepository
import com.kreativesquadz.hisabkitab.repository.SavedOrderRepository
import com.kreativesquadz.hisabkitab.repository.SettingsRepository
import com.kreativesquadz.hisabkitab.repository.UserSettingRepository
import com.kreativesquadz.hisabkitab.worker.UpdateInvoiceWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class EditBillDetailsViewModel @Inject constructor(val workManager: WorkManager,
                                                   val billHistoryRepository: BillHistoryRepository,
                                                   val userSettingRepository: UserSettingRepository,
                                                   val inventoryRepository: InventoryRepository,
                                                   val creditNoteRepository: CreditNoteRepository,
                                                   val savedOrderRepository: SavedOrderRepository,
                                                   val settingsRepository: SettingsRepository,
                                                   val customerManagRepository: CustomerManagRepository
)
    : ViewModel() {



    private val _invoiceID = MutableLiveData<Long?>()

    lateinit var userSetting : LiveData<UserSetting>
    val invoiceId: LiveData<Long?> get() = _invoiceID

    val _invoiceData = MutableLiveData<Invoice>()
    val invoiceData: LiveData<Invoice> = _invoiceData

    val _invoiceItems = MutableLiveData<List<InvoiceItem>>()
    val invoiceItems: LiveData<List<InvoiceItem>> get() = _invoiceItems

    private val _allDataReady = MutableLiveData<Boolean>()
    val allDataReady: LiveData<Boolean> = _allDataReady

    fun fetchAllDetails(invoiceId: String) = viewModelScope.launch {
        try {
            val invoiceDetailsDeferred = async { billHistoryRepository.getInvoiceByInvoiceId(invoiceId.toInt()).asFlow().first() }
            val invoiceItemsDeferred = async { billHistoryRepository.getInvoiceItems(invoiceId.toLong())}

            val invoiceDetails = invoiceDetailsDeferred.await()
            val invoiceItems = invoiceItemsDeferred.await()


            _invoiceData.value = invoiceDetails
            _invoiceItems.value = invoiceItems

            checkIfAllDataReady()
        } catch (e: Exception) {
            // Handle exceptions
        }
    }
    private fun checkIfAllDataReady() {
        if (_invoiceData.value != null && _invoiceItems.value != null) {
            _allDataReady.value = true
        }
    }

    fun updateInvoiceWithItems(invoice: Invoice, items: List<InvoiceItem>,invoiceId: Long)  {
        try {
            viewModelScope.launch{
               val updated = billHistoryRepository. updateInvoiceWithItems(invoice, items)
                Log.e("updated", updated.toString())
                if (updated){
                    _invoiceID.value = 1
                    invoice.creditNoteId?.let {
                        creditNoteRepository.redeemCreditNoteById(it)
                    }
                    updateInvoiceWorker(invoiceId)
                }else{
                    _invoiceID.value = 0
                }

            }
        } catch (e: Exception) {
            // Handle exception, e.g., show a message to the user
        }
    }

    fun getCustomerById(customerId: String): LiveData<Customer> = liveData {
        val customer = customerManagRepository.getCustomer(customerId) // suspend function
        emit(customer)
    }

    fun getCreditNoteById(creditNoteId: Long): LiveData<CreditNote> = liveData {
       val creditNote = creditNoteRepository.getCreditNoteById(creditNoteId) // suspend function
       if (creditNote != null) {
           emit(creditNote)
       }
   }

    fun clearInvoiceStatus() {
        _invoiceID.value = null
    }

    private fun updateInvoiceWorker(id: Long) {
        val data = Data.Builder()
            .putLong("id", id)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequestInvoiceUpdate = OneTimeWorkRequestBuilder<UpdateInvoiceWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        workManager.enqueueUniqueWork(
            "syncWorkRequestInvoiceUpdate",
            ExistingWorkPolicy.KEEP,
            syncWorkRequestInvoiceUpdate
        )
    }

    fun getUserSettings(): LiveData<UserSetting> {
        userSetting = userSettingRepository.getUserSetting(Config.userId)
        return userSetting
    }

}