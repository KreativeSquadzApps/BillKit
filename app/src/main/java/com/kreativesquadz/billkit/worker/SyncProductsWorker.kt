package com.kreativesquadz.billkit.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.repository.InventoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class SyncProductsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
   private val repository: InventoryRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val unsyncedProducts = repository.getUnsyncedProducts()
            for (product in unsyncedProducts) {
                val response = ApiClient.getApiService().addProduct(product.copy(isSynced = 1))
                if (response.body()?.message.toString().equals("Product inserted successfully")) {
                    repository.markproductsAsSynced(product.copy(isSynced = 1))
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
