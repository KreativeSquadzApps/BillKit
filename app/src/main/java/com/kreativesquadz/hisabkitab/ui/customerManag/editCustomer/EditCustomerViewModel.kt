package com.kreativesquadz.hisabkitab.ui.customerManag.editCustomer

import android.content.Context
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
import com.kreativesquadz.hisabkitab.model.Customer
import com.kreativesquadz.hisabkitab.repository.CustomerManagRepository
import com.kreativesquadz.hisabkitab.worker.UpdaterCustomerDetailsWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditCustomerViewModel @Inject constructor(private val customerManagRepository: CustomerManagRepository  )
    : ViewModel() {

    private var _customerStatus = MutableLiveData<Boolean>()
    val customerStatus: LiveData<Boolean> get() = _customerStatus

    fun addCustomerObj(customer: Customer?, context: Context) {
        viewModelScope.launch {
            customer?.let {
                _customerStatus.value = customerManagRepository.updateCustomer(it).value
                scheduleCustomerDetailsUpdate(context , it.id.toString())
            }

        }
    }

    fun scheduleCustomerDetailsUpdate(context: Context, id: String) {
        val data = Data.Builder()
            .putString("id",id)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<UpdaterCustomerDetailsWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "scheduleCustomerDetailsUpdate",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }


    }