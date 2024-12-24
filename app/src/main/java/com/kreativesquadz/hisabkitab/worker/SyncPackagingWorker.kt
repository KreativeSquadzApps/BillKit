package com.kreativesquadz.hisabkitab.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.hisabkitab.api.ApiClient
import com.kreativesquadz.hisabkitab.repository.BillSettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class SyncPackagingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
   private val repository: BillSettingsRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val unsyncedPackaging = repository.getUnsyncedPackaging()
            for (packaging in unsyncedPackaging) {
                val response = ApiClient.getApiService().addPackaging(packaging.copy(isSynced = 1))
                if (response.body()?.invoiceId == 200) {
                    repository.markPackagingAsSynced(packaging)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
