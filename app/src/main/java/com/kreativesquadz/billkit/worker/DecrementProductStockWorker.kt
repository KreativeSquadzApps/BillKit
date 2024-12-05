package com.kreativesquadz.billkit.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.model.request.CreditNoteStatusUpdate
import com.kreativesquadz.billkit.model.request.InvoiceRequest
import com.kreativesquadz.billkit.model.request.InvoiceStatusUpdate
import com.kreativesquadz.billkit.model.request.ProductStockRequest
import com.kreativesquadz.billkit.model.request.ProductStockRequestByName
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class DecrementProductStockWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val name = inputData.getString("name") ?: ""
            val stock = inputData.getInt("stock", 0)
            ApiClient.getApiService().decrementProductStockByName(ProductStockRequestByName(name, stock))
            Log.e("DecrementPtoductWorker","DecrementPtoductWorker")
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}