package com.kreativesquadz.billkit.ui.home.billDetails

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
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.ApiStatus
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.model.UserSetting
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CreditNoteRepository
import com.kreativesquadz.billkit.repository.InventoryRepository
import com.kreativesquadz.billkit.repository.SavedOrderRepository
import com.kreativesquadz.billkit.repository.SettingsRepository
import com.kreativesquadz.billkit.repository.UserSettingRepository
import com.kreativesquadz.billkit.worker.SyncInvoicesWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class BillDetailsViewModel @Inject constructor(val billHistoryRepository: BillHistoryRepository,
                                               val userSettingRepository: UserSettingRepository,
                                               val inventoryRepository: InventoryRepository,
                                               val creditNoteRepository: CreditNoteRepository,
                                               val savedOrderRepository: SavedOrderRepository)
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
                val invoiceId =  billHistoryRepository.insertInvoiceWithItems(invoice, items)
                creditNoteRepository.redeemCreditNoteById(creditNoteId)
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
            ExistingWorkPolicy.KEEP,
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


}