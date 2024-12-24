package com.kreativesquadz.hisabkitab.ui.customerManag.customerDetails

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kreativesquadz.hisabkitab.repository.CustomerManagRepository
import com.kreativesquadz.hisabkitab.worker.DeleteCustomerWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerDetailsViewModel @Inject constructor(private val customerRepository: CustomerManagRepository) : ViewModel() {

    fun deleteCustomer(context: Context, id : Long){
        viewModelScope.launch {
            customerRepository.deleteCustomer(id)
            deleteCustomerWork(context, id.toString())
        }
    }

    private fun deleteCustomerWork (context: Context, id: String ){
        val data = Data.Builder()
            .putString("id",id)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<DeleteCustomerWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "deleteCustomerWork",
            ExistingWorkPolicy.APPEND,
            syncWorkRequest
        )
    }

}