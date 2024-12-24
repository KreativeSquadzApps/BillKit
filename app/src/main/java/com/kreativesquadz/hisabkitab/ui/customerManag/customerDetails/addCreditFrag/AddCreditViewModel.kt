package com.kreativesquadz.hisabkitab.ui.customerManag.customerDetails.addCreditFrag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kreativesquadz.hisabkitab.repository.CreditRepository
import com.kreativesquadz.hisabkitab.repository.CustomerManagRepository
import com.kreativesquadz.hisabkitab.worker.SyncCustomerCreditWorker
import com.kreativesquadz.hisabkitab.worker.UpdateCustomerCreditWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddCreditViewModel @Inject constructor(private val workManager: WorkManager,
                                             private val creditRepository: CreditRepository,
                                             private val customerManagRepository: CustomerManagRepository) : ViewModel() {




    fun updateCreditAmount(id : Long ,credit : Double,type: String){
        viewModelScope.launch {
            customerManagRepository.updateCreditAmount(id,credit,type)
            updateCreditWork(id.toString(),credit,type)
            updateCustomerCreditDetailsWork(id.toString(),credit)
        }
    }




    private fun updateCreditWork (id: String, credit: Double,type : String) {
        val data = Data.Builder()
            .putString("id",id)
            .putString("type",type)
            .putDouble("credit",credit)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<UpdateCustomerCreditWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        workManager.enqueueUniqueWork("updateCreditWork",
            ExistingWorkPolicy.APPEND,
            syncWorkRequest)

    }

    private fun updateCustomerCreditDetailsWork (id: String,credit: Double) {
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

        workManager.enqueueUniqueWork("updateCustomerCreditDetailsWork",
        ExistingWorkPolicy.APPEND,
        syncWorkRequest)

    }
}