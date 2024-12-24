package com.kreativesquadz.hisabkitab.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.hisabkitab.api.ApiClient
import com.kreativesquadz.hisabkitab.model.request.InvoiceRequest
import com.kreativesquadz.hisabkitab.repository.BillHistoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class UpdateInvoiceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: BillHistoryRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val id = inputData.getLong("id", 0)
            val invoice = repository.getInvoiceByIdWithoutLiveData(id.toInt())
            val items = repository.getInvoiceItems(invoice.invoiceId.toLong())
            ApiClient.getApiService().updateInvoice(InvoiceRequest(invoice, items))
            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
