package com.kreativesquadz.billkit.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.repository.InventoryRepository
import com.kreativesquadz.billkit.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class AddInvoicePrefixWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: SettingsRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val id = inputData.getLong("id",0)
            val invoicePrefixNumber = repository.getInvoicePrefixNumber(id)
           val response = ApiClient.getApiService().addInvoicePrefixNumber(invoicePrefixNumber)
            Log.d("AddInvoicePrefixWorker", "reeeee: $response")
            Result.success()
        } catch (e: Exception) {
            Log.d("AddInvoicePrefixWorker", "doWork: $e")
            Result.retry()
        }
    }
}
