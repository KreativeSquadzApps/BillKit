package com.kreativesquadz.hisabkitab.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.hisabkitab.api.ApiClient
import com.kreativesquadz.hisabkitab.model.request.InvoiceStatusUpdate
import com.kreativesquadz.hisabkitab.repository.BillHistoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class UpdateInvoiceStatusWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
   private val repository: BillHistoryRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val invoiceId = inputData.getString("invoiceId") ?: ""
            val status = inputData.getString("status") ?: ""
               val invoiceStatusUpdate = InvoiceStatusUpdate(invoiceId.toLong(),status)
                val response = ApiClient.getApiService().updateInvoiceStatus(invoiceStatusUpdate)
            Log.d("sommeeeeee", invoiceId +"    " + status)
            if (response.body()?.message.toString().equals("invoice status updated successfully")) {
                    Log.d("UpdateInvoiceStatusWorker", "Invoice status updated successfully")
                }

            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
