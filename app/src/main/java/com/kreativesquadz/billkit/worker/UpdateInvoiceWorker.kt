package com.kreativesquadz.billkit.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.model.request.InvoiceRequest
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.InventoryRepository
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
