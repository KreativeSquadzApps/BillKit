package com.kreativesquadz.hisabkitab.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.hisabkitab.api.ApiClient
import com.kreativesquadz.hisabkitab.repository.StaffManagRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class UpdaterStaffDetailsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
   private val repository: StaffManagRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
              val id = inputData.getString("id") ?: ""
               val staff = repository.getStaffById(id.toLong())
                val response = ApiClient.getApiService().updateStaff(staff)
                if (response.body()?.message.toString().equals("Staff Updated successfully")) {
                }

            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
