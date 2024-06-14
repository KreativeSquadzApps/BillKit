package com.kreativesquadz.billkit.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.repository.InventoryRepository
import com.kreativesquadz.billkit.repository.UserSettingRepository
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
            Log.e("SyncUserSettingWorker", "ssssssss")

            val userSettingObj = repository.loadUserSetting(Config.userId)

             val response = ApiClient.getApiService().updateUserSetting(userSettingObj.value?.data)
                if (response.body()?.message.toString().equals("User settings updated successfully")) {
                        Log.e("SyncUserSettingWorker", "success")
                }

            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
