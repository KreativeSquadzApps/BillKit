package com.kreativesquadz.billkit.ui.home.billDetails

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
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.model.settings.UserSetting
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CreditNoteRepository
import com.kreativesquadz.billkit.repository.InventoryRepository
import com.kreativesquadz.billkit.repository.SavedOrderRepository
import com.kreativesquadz.billkit.repository.SettingsRepository
import com.kreativesquadz.billkit.repository.UserSettingRepository
import com.kreativesquadz.billkit.worker.SyncInvoicesWorker
import com.kreativesquadz.billkit.worker.UpdateInvoicePrefixIncrementWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class BillDetailsViewModel @Inject constructor(val workManager: WorkManager,
                                               val billHistoryRepository: BillHistoryRepository,
                                               val userSettingRepository: UserSettingRepository,
                                               val inventoryRepository: InventoryRepository,
                                               val creditNoteRepository: CreditNoteRepository,
                                               val savedOrderRepository: SavedOrderRepository,
                                               val settingsRepository: SettingsRepository)
    : ViewModel() {



    private val _invoiceID = MutableLiveData<Long?>()

    lateinit var userSetting : LiveData<UserSetting>
    val invoiceId: LiveData<Long?> get() = _invoiceID

//    fun addInvoice(creditNoteId : Int? ,invoice: Invoice,context: Context) {
//        viewModelScope.launch {
//            _invoiceID.value = billHistoryRepository.addInvoice(invoice)
//
//            creditNoteRepository.redeemCreditNoteById(creditNoteId)
//            invoice.invoiceItems.forEach{
//                inventoryRepository.decrementProductStock(it.itemName.split(" ")[0],it.quantity)
//            }
//            scheduleInvoiceSync(context)
//            _invoiceStatus.value = true
//        }
//
//    }
    fun insertInvoiceWithItems(isSavedOrderIdExist: Long?,invoice: Invoice, items: List<InvoiceItem>,creditNoteId : Int?,context: Context)  {
        try {
            viewModelScope.launch{
                if(isSavedOrderIdExist != null){
                    deleteSavedOrder(isSavedOrderIdExist)
                }
                Log.e("BBBinvoice", invoice.toString())

                val invoiceId =  billHistoryRepository.insertInvoiceWithItems(invoice, items)
                creditNoteRepository.redeemCreditNoteById(creditNoteId)
                items.forEach{
                    Log.e("NNNNNN", it.quantity.toString())
                    inventoryRepository.decrementProductStock(it.itemName.split("(")[0],it.quantity)
                }
                _invoiceID.value = invoiceId
                scheduleInvoiceSync(context)
            }


        } catch (e: Exception) {
            // Handle exception, e.g., show a message to the user
        }
    }

    fun clearInvoiceStatus() {
        _invoiceID.value = null
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
            ExistingWorkPolicy.APPEND,
            syncWorkRequest
        )
    }

    fun getUserSettings(): LiveData<UserSetting> {
        userSetting = userSettingRepository.getUserSetting(Config.userId)
        return userSetting
    }


    fun deleteSavedOrder(orderId: Long) {
            savedOrderRepository.deleteSavedOrder(orderId)

    }

    private fun updateInvoicePrefixNumberWorker(id: Long) {
        val data = Data.Builder()
            .putLong("id", id)
            .build()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequestsss = OneTimeWorkRequestBuilder<UpdateInvoicePrefixIncrementWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        workManager.enqueueUniqueWork(
            "UpdateInvoicePrefixIncrementWorker",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequestsss
        )
    }

    fun updateInvoicePrefixNumber(invoicePrefix: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
               val invoicePrefixNumber = settingsRepository.getInvoicePrefixNumberWithPrefix(invoicePrefix)
                Log.d("ViewModel", "InvoicePrefixNumber: ${invoicePrefixNumber}")
                settingsRepository.updateInvoiceNumberAndPrefix(Config.userId,
                    invoicePrefixNumber.id.toLong(),
                    invoicePrefixNumber.invoicePrefix,
                    invoicePrefixNumber.invoiceNumber.toInt()+1)
                updateInvoicePrefixNumberWorker(invoicePrefixNumber.id.toLong())
            } catch (e: Exception) {
                // Handle error or log it
                Log.e("ViewModel", "Error: ${e.message}")
            }
        }
    }
}