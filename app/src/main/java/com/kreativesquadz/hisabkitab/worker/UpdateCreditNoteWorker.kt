package com.kreativesquadz.hisabkitab.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.hisabkitab.api.ApiClient
import com.kreativesquadz.hisabkitab.model.request.CreditNoteStatusUpdate
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class UpdateCreditNoteWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val id = inputData.getString("id") ?: ""
            val status = inputData.getString("status") ?: ""
               val creditNoteRequest = CreditNoteStatusUpdate(id.toInt(),status)
                val response = ApiClient.getApiService().updateCreditNoteStatus(creditNoteRequest)
            if (response.body()?.message.toString().equals("credit status updated successfully")) {
                    Log.d("UpdateInvoiceStatusWorker", "credit status updated successfully")
                }

            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
