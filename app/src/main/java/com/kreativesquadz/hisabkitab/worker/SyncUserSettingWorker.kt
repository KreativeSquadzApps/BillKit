package com.kreativesquadz.hisabkitab.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.api.ApiClient
import com.kreativesquadz.hisabkitab.repository.UserSettingRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class SyncUserSettingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
   private val repository: UserSettingRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val userSettingObj = repository.getUserSettingById(Config.userId)
               val response = ApiClient.getApiService().updateUserSetting(userSettingObj)
                if (response.body()?.message.toString().equals("User settings updated successfully")) {
                }

            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
