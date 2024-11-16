package com.kreativesquadz.billkit.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.kreativesquadz.billkit.Dao.CreditNoteDao
import com.kreativesquadz.billkit.Dao.InvoiceDao
import com.kreativesquadz.billkit.Database.AppDatabase
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.api.ApiResponse
import com.kreativesquadz.billkit.api.common.NetworkBoundResource
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.CreditNote
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreditNoteRepository @Inject constructor(val db: AppDatabase) {
    private val creditNoteDao : CreditNoteDao = db.creditNoteDao()
    private val invoiceDao : InvoiceDao = db.invoiceDao()
    suspend fun addCreditNote(creditNote: CreditNote) {
        creditNoteDao.insertCreditNote(creditNote)
        val statusLiveData = MutableLiveData<Boolean>()
        statusLiveData.value = true
    }
    suspend fun updateCreditNote(creditNote: CreditNote) {
        creditNoteDao.updateCreditNote(creditNote)
        val statusLiveData = MutableLiveData<Boolean>()
        statusLiveData.value = true
    }

    suspend fun getCreditNoteByInvoiceId(invoiceId: Long) : CreditNote?  {
       return creditNoteDao.getCreditNoteByInvoiceId(invoiceId)
    }
    suspend fun getCreditNoteById(id: Long) : CreditNote?  {
        return creditNoteDao.getCreditNoteById(id)
    }

    suspend fun getUnsyncedCreditNote(): List<CreditNote> {
        return creditNoteDao.getUnsyncedCreditNote()
    }

    suspend fun markCreditNoteAsSynced(creditNote: CreditNote) {
        creditNoteDao.updateCreditNote(creditNote)
    }

    suspend fun updateInvoiceStatus(invoiceId: Int) {
        creditNoteDao.updateInvoiceStatus(invoiceId)
    }

    suspend fun redeemCreditNoteById(id: Int?) {
        creditNoteDao.updateCreditNoteStatus(id, "Cleared")
    }
    fun loadAllCreditNote(userId:Long): LiveData<Resource<List<CreditNote>>> {
        return object : NetworkBoundResource<List<CreditNote>, List<CreditNote>>() {
            override fun saveCallResult(item: List<CreditNote>) {
                try {
                    item.forEach {
                        it.invoiceItems = invoiceDao.getInvoiceItems(it.invoiceId)
                    }
                    db.runInTransaction {
                        creditNoteDao.deleteCreditNoteList()
                        creditNoteDao.insertCreditNoteList(item)

                    }
                } catch (ex: Exception) {
                    Log.e("TAG", ex.toString())
                }
            }

            override fun shouldFetch(data: List<CreditNote>?): Boolean {
                return true
            }

            override fun loadFromDb(): LiveData<List<CreditNote>> {
                return creditNoteDao.getCreditNotesByUser(userId)
            }

            override fun createCall(): LiveData<ApiResponse<List<CreditNote>>> {
                return ApiClient.getApiService().loadCreditNote(userId)
            }
        }.asLiveData()
    }

    fun getPagedCreditNoteFromDb(startDate: Long, endDate: Long): Flow<PagingData<CreditNote>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20, // Define the size of each page
                enablePlaceholders = false
            ),
            pagingSourceFactory = { creditNoteDao.getPagedCreditNoteByDateRange(startDate, endDate) }
        ).flow
    }

}