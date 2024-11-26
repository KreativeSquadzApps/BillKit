package com.kreativesquadz.billkit.ui.customerManag.customerDetails.addCreditFrag

import android.content.Context
import androidx.camera.core.impl.utils.ContextUtil.getApplicationContext
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
import com.kreativesquadz.billkit.utils.enqueueWork
import com.kreativesquadz.billkit.worker.SyncCustomerCreditWorker
import com.kreativesquadz.billkit.worker.UpdateCustomerCreditWorker
import com.kreativesquadz.billkit.worker.UpdateProductStockWorker
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