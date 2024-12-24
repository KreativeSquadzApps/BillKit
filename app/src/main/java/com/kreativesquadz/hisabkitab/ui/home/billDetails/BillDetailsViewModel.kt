package com.kreativesquadz.hisabkitab.ui.home.billDetails

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
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.api.common.common.Resource
import com.kreativesquadz.hisabkitab.model.Invoice
import com.kreativesquadz.hisabkitab.model.InvoiceItem
import com.kreativesquadz.hisabkitab.model.settings.UserSetting
import com.kreativesquadz.hisabkitab.repository.BillHistoryRepository
import com.kreativesquadz.hisabkitab.repository.CreditNoteRepository
import com.kreativesquadz.hisabkitab.repository.InventoryRepository
import com.kreativesquadz.hisabkitab.repository.SavedOrderRepository
import com.kreativesquadz.hisabkitab.repository.SettingsRepository
import com.kreativesquadz.hisabkitab.repository.UserSettingRepository
import com.kreativesquadz.hisabkitab.worker.DecrementProductStockWorker
import com.kreativesquadz.hisabkitab.worker.SyncInvoicesWorker
import com.kreativesquadz.hisabkitab.worker.UpdateInvoicePrefixIncrementWorker
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
    lateinit var invoicess : LiveData<Resource<List<Invoice>>>

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
    fun insertInvoiceWithItems(isSavedOrderIdExist: Long?,invoice: Invoice, items: List<InvoiceItem>,creditNoteId : Int?)  {
        try {
            viewModelScope.launch{
                Log.e("BBBinvoice", invoice.toString())
                val invoiceId =  billHistoryRepository.insertInvoiceWithItems(invoice, items)
                creditNoteRepository.redeemCreditNoteById(creditNoteId)
                items.forEach{
                    val productName = it.itemName.split("(")[0].trim()
                    val quantity = it.quantity
                    Log.e("NNNNNN", it.quantity.toString())
                    val isAvailable = inventoryRepository.isProductAvailable(productName)
                    if (isAvailable){
                        inventoryRepository.decrementProductStock(productName,quantity)
                        decrementProductStockWork(productName,quantity)
                    }
                }

                _invoiceID.value = invoiceId
                scheduleInvoiceSync()
                isSavedOrderIdExist?.let {
                    deleteSavedOrder(isSavedOrderIdExist)
                }
            }


        } catch (e: Exception) {
            // Handle exception, e.g., show a message to the user
        }
    }
    private fun fetchAllInvoices() {
        invoicess = billHistoryRepository.loadAllInvoices()
    }
    fun clearInvoiceStatus() {
        _invoiceID.value = null
    }


    fun getUserSettings(): LiveData<UserSetting> {
        userSetting = userSettingRepository.getUserSetting(Config.userId)
        return userSetting
    }


    fun deleteSavedOrder(orderId: Long) {
            savedOrderRepository.deleteSavedOrder(orderId)

    }


    fun updateInvoicePrefixNumber(invoicePrefix: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
               val invoicePrefixNumber = settingsRepository.getInvoicePrefixNumberWithPrefix(invoicePrefix)
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


    private fun scheduleInvoiceSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncInvoicesWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "invoiceSyncWork",
            ExistingWorkPolicy.APPEND,
            syncWorkRequest
        )
    }

    private fun decrementProductStockWork (name: String , stock : Int ) {
        val data = Data.Builder()
            .putString("name",name)
            .putInt("stock",stock)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<DecrementProductStockWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()


        workManager.enqueueUniqueWork(
            "decrementProductStockWork",
            ExistingWorkPolicy.APPEND,
            syncWorkRequest
        )
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


}