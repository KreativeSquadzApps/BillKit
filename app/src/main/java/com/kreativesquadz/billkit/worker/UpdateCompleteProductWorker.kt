package com.kreativesquadz.billkit.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.repository.InventoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class UpdateCompleteProductWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: InventoryRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("UpdateCompleteProductWorker", "started")
        return try {
            val id = inputData.getString("id") ?: ""
            val product = repository.getProduct(id.toLong())
            ApiClient.getApiService().updateProduct(product)
            Log.d("UpdateCompleteProductWorker", product.toString())
            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
