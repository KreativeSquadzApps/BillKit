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
class AddCompanyDetailsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: SettingsRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val companyDetails = repository.getCompanyDetails(Config.userId)
            ApiClient.getApiService().addCompanyDetails(companyDetails)
            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
