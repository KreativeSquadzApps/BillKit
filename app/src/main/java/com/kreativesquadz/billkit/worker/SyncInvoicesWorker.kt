package com.kreativesquadz.billkit.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.model.request.InvoiceRequest
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class SyncInvoicesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
   private val repository: BillHistoryRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {

            val unsyncedInvoices = repository.getUnsyncedInvoices()
            for (invoice in unsyncedInvoices) {
                val items = repository.getInvoiceItems(invoice.invoiceId.toLong())
                items.forEach {
                    it.invoiceId = invoice.invoiceId.toLong()
                }
                val invoiceRequest = InvoiceRequest(invoice.copy(isSynced = 1), items)
                val response = ApiClient.getApiService().createInvoice(invoiceRequest)
                if (response.body()?.message.toString().equals("Invoice added successfully")) {
                    repository.markInvoiceAsSynced(invoice)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
