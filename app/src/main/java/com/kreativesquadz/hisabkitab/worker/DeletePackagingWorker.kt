package com.kreativesquadz.hisabkitab.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.hisabkitab.api.ApiClient
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class DeletePackagingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val id = inputData.getInt("id",0)
            ApiClient.getApiService().deletePackaging(id)
            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
