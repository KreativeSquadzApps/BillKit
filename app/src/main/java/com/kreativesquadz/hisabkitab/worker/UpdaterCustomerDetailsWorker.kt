package com.kreativesquadz.hisabkitab.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.hisabkitab.api.ApiClient
import com.kreativesquadz.hisabkitab.repository.CustomerManagRepository
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
