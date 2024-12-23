package com.kreativesquadz.billkit.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.repository.InventoryRepository
import com.kreativesquadz.billkit.repository.StaffManagRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class SyncStaffWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
   private val repository: StaffManagRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val unsyncedStaff = repository.getUnsyncedStaff()
            for (staff in unsyncedStaff) {
                Log.e("unsyncedStaff",staff.toString())

                val response = ApiClient.getApiService().addStaff(staff.copy(isSynced = 1))
                if (response.body()?.message.toString() == "User added successfully") {
                    repository.markstaffAsSynced(staff.copy(isSynced = 1))
                    Log.e("unsyncedStaffadded","unsyncedStaffadded")

                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
