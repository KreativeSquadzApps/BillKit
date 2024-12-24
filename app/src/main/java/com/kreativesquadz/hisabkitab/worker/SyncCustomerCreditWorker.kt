package com.kreativesquadz.hisabkitab.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.hisabkitab.api.ApiClient
import com.kreativesquadz.hisabkitab.repository.CreditRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class SyncCustomerCreditWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
   private val repository: CreditRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val id = inputData.getString("id") ?: ""
            val type = inputData.getString("type") ?: ""
            val creditedAmount = inputData.getDouble("credit", 0.0)

            val customerCreditDetails = repository.getCustomerCreditDetails(id.toLong())
             val response = ApiClient.getApiService().addCustomerCreditDetail(customerCreditDetails.copy(creditAmount = creditedAmount, creditType = type))
                if (response.body()?.message.toString().equals("Customer credit detail added successfully")) {
                    //repository.markCustomerAsSynced(customer)
                 }

            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
