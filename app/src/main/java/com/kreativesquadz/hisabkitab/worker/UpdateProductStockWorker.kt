package com.kreativesquadz.hisabkitab.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.hisabkitab.api.ApiClient
import com.kreativesquadz.hisabkitab.model.request.ProductStockRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class UpdateProductStockWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val id = inputData.getString("id") ?: ""
            val stock = inputData.getInt("stock", 0)
            ApiClient.getApiService().updateProductStock(ProductStockRequest(id.toLong(), stock))
            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
