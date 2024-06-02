package com.kreativesquadz.billkit.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.repository.InventoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class SyncCategoriesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
   private val repository: InventoryRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val unsyncedCategory = repository.getUnsyncedCategories()
            for (category in unsyncedCategory) {
                val response = ApiClient.getApiService().addCategories(category.copy(isSynced = 1))
                if (response.body()?.message.toString().equals("Category added successfully")) {
                    repository.markCategoriesAsSynced(category.copy(isSynced = 1))
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
