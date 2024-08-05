package com.kreativesquadz.billkit.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import com.kreativesquadz.billkit.repository.GstTaxRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class SyncGstWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
   private val repository: GstTaxRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val unsyncedGst = repository.getUnsyncedGst()
            for (gst in unsyncedGst) {
                val response = ApiClient.getApiService().addGstTax(gst.copy(isSynced = 1))
                if (response.body()?.message.toString().equals("Gst inserted successfully")) {
                    repository.markGstAsSynced(gst)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
