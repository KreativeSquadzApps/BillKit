package com.kreativesquadz.billkit.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class UpdaterCustomerDetailsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
   private val repository: CustomerManagRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
              val id = inputData.getString("id") ?: ""
               val customer = repository.getCustomer(id)
                val response = ApiClient.getApiService().updateCustomer(customer)
                if (response.body()?.message.toString().equals("Customer Updated successfully")) {
                    repository.markCustomerAsSynced(customer)
                }

            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
