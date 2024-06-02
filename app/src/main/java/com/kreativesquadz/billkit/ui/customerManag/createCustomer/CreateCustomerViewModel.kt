package com.kreativesquadz.billkit.ui.customerManag.createCustomer

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
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import com.kreativesquadz.billkit.worker.SyncCustomerWorker
import com.kreativesquadz.billkit.worker.SyncInvoicesWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateCustomerViewModel @Inject constructor(val repository: CustomerManagRepository) : ViewModel() {
    private var _customerStatus = MutableLiveData<Boolean>()
    val customerStatus: LiveData<Boolean> get() = _customerStatus

    fun addCustomerObj(customer: Customer, context: Context) {
        viewModelScope.launch {
            _customerStatus.value = repository.addCustomer(customer).value
            scheduleCustomerSync(context)
        }
    }

    fun scheduleCustomerSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncCustomerWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "customerSyncWork",
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )
    }
}