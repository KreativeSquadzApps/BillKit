package com.kreativesquadz.billkit.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.repository.InventoryRepository
import com.kreativesquadz.billkit.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class UpdateInvoicePrefixWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: SettingsRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val id = inputData.getLong("id",0)
            val invoicePrefixWorker = repository.getInvoicePrefixNumber(id)
            ApiClient.getApiService().updateCompanyInvoicePrefixNumber(invoicePrefixWorker)
            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
