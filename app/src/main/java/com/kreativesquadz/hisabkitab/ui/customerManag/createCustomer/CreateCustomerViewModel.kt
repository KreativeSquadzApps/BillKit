package com.kreativesquadz.hisabkitab.ui.customerManag.createCustomer

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
import com.kreativesquadz.hisabkitab.model.Customer
import com.kreativesquadz.hisabkitab.repository.CustomerManagRepository
import com.kreativesquadz.hisabkitab.worker.SyncCustomerWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateCustomerViewModel @Inject constructor(val repository: CustomerManagRepository) : ViewModel() {
    private val _customerStatus = MutableLiveData<String>()
    val customerStatus: LiveData<String> get() = _customerStatus




    fun addCustomerObj(customer: Customer, context: Context, onResult: (Customer?) -> Unit) {
        viewModelScope.launch {
           val isCustomer = repository.addCustomer(customer)
            if (isCustomer == null) {
                _customerStatus.value = "Customer with this name or number already exists"
                return@launch
            } else {
                _customerStatus.value = "Customer added successfully"
                scheduleCustomerSync(context)
                val syncedCustomer = repository.getCustomerByName(customer.customerName)
                onResult(syncedCustomer)
            }

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