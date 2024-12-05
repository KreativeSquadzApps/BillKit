package com.kreativesquadz.billkit.ui.customerManag.createCustomer

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import com.kreativesquadz.billkit.worker.SyncCustomerWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateCustomerViewModel @Inject constructor(val repository: CustomerManagRepository) : ViewModel() {
    private val _customerStatus = MutableLiveData<String>()
    val customerStatus: LiveData<String> get() = _customerStatus

    private var _customer = MutableLiveData<Resource<List<Customer>>>()
    val customer: LiveData<Resource<List<Customer>>> get() = _customer


    fun addCustomerObj(customer: Customer, context: Context, onResult: (Customer?) -> Unit) {
        viewModelScope.launch {
            repository.addCustomer(customer)
            scheduleCustomerSync(context)

            val result = repository.loadAllCustomers()
                .asFlow()
                .first { resource ->
                    resource.status.name == "SUCCESS"  // Compare with hardcoded string
                }

            val syncedCustomer = repository.getCustomerByName(customer.customerName)
            onResult(syncedCustomer)
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