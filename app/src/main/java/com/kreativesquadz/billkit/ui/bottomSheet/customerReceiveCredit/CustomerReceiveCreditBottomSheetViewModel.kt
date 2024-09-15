package com.kreativesquadz.billkit.ui.bottomSheet.customerReceiveCredit

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import com.kreativesquadz.billkit.worker.SyncCustomerCreditWorker
import com.kreativesquadz.billkit.worker.UpdateCustomerCreditWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerReceiveCreditBottomSheetViewModel @Inject constructor(val customerManagRepository: CustomerManagRepository) : ViewModel() {



    fun updateCreditAmount(context: Context, id : Long, credit : Double, type : String){
        viewModelScope.launch {
            customerManagRepository.removeCreditAmount(id,credit,type)
            updateCreditWork(context, id.toString(),credit,type)
            updateCustomerCreditDetailsWork(context,id.toString(),credit,type)
        }
    }




    private fun updateCreditWork (context: Context, id: String, credit: Double,type: String ) {
        val data = Data.Builder()
            .putString("id",id)
            .putDouble("credit",credit)
            .putString("type",type)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<UpdateCustomerCreditWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "updateCreditWork",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }

    private fun updateCustomerCreditDetailsWork (context: Context, id: String, credit: Double,type: String ) {
        val data = Data.Builder()
            .putString("id",id)
            .putDouble("credit",credit)
            .putString("type",type)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncCustomerCreditWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "updateCustomerCreditDetailsWork",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }
}