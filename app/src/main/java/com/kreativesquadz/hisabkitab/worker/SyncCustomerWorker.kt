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
class SyncCustomerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
   private val repository: CustomerManagRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val unsyncedCustomers = repository.getUnsyncedCustomers()
            for (customer in unsyncedCustomers) {
                val response = ApiClient.getApiService().addCustomer(customer.copy(isSynced = 1))
                if (response.body()?.message.toString().equals("Customer inserted successfully")) {
                    repository.markCustomerAsSynced(customer)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
