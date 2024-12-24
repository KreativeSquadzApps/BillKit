package com.kreativesquadz.hisabkitab.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.hisabkitab.api.ApiClient
import com.kreativesquadz.hisabkitab.model.request.CustomerCreditRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class UpdateCustomerCreditWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val id = inputData.getString("id") ?: ""
            val type = inputData.getString("type") ?: ""
            val creditedAmount = inputData.getDouble("credit", 0.0)
            ApiClient.getApiService().updateCustomerCredit(CustomerCreditRequest(id.toLong(), creditedAmount, type))
            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
