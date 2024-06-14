package com.kreativesquadz.billkit.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.repository.CreditNoteRepository
import com.kreativesquadz.billkit.repository.InventoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class SyncCreditNoteWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
   private val repository: CreditNoteRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val unsyncedProducts = repository.getUnsyncedCreditNote()
            for (creditNote in unsyncedProducts) {
                val response = ApiClient.getApiService().addCreditNote(creditNote.copy(isSynced = 1))
                if (response.body()?.message.toString().equals("Credit note inserted successfully")) {
                    repository.markCreditNoteAsSynced(creditNote.copy(isSynced = 1))
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()

        }
    }
}
