package com.kreativesquadz.billkit.ui.customerManag.customerDetails.addCreditFrag

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kreativesquadz.billkit.repository.CreditRepository
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import com.kreativesquadz.billkit.worker.SyncCustomerCreditWorker
import com.kreativesquadz.billkit.worker.UpdateCustomerCreditWorker
import com.kreativesquadz.billkit.worker.UpdateProductStockWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddCreditViewModel @Inject constructor(private val creditRepository: CreditRepository,
                                             private val customerManagRepository: CustomerManagRepository) : ViewModel() {




    fun updateCreditAmount( context: Context,id : Long ,credit : Double){
        viewModelScope.launch {
            customerManagRepository.updateCreditAmount(id,credit)
            updateCreditWork(context, id.toString(),credit)
            updateCustomerCreditDetailsWork(context,id.toString(),credit)
        }
    }




    private fun updateCreditWork (context: Context, id: String, credit: Double ) {
        val data = Data.Builder()
            .putString("id",id)
            .putDouble("credit",credit)
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

    private fun updateCustomerCreditDetailsWork (context: Context, id: String,credit: Double) {
        val data = Data.Builder()
            .putString("id",id)
            .putString("type","Manual")
            .putDouble("credit",credit)
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