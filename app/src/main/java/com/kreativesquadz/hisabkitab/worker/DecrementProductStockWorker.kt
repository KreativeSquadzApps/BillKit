package com.kreativesquadz.hisabkitab.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.hisabkitab.api.ApiClient
import com.kreativesquadz.hisabkitab.model.request.ProductStockRequestByName
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class DecrementProductStockWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val name = inputData.getString("name") ?: ""
            val stock = inputData.getInt("stock", 0)
            ApiClient.getApiService().decrementProductStockByName(ProductStockRequestByName(name, stock))
            Log.e("DecrementPtoductWorker","DecrementPtoductWorker")
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
