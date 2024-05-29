package com.kreativesquadz.billkit.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kreativesquadz.billkit.Dao.InvoiceDao
import com.kreativesquadz.billkit.Database.AppDatabase
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.api.ApiResponse
import com.kreativesquadz.billkit.api.common.NetworkBoundResource
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Invoice
import javax.inject.Inject

class BillHistoryRepository @Inject constructor(private val db: AppDatabase) {
    private val invoiceDao : InvoiceDao = db.invoiceDao()
    fun loadAllInvoices(): LiveData<Resource<List<Invoice>>> {
        return object : NetworkBoundResource<List<Invoice>, List<Invoice>>() {
            override fun saveCallResult(item: List<Invoice>) {
                try {
                    db.runInTransaction {
                        // Clear existing data
                        invoiceDao.deleteInvoices()
                        invoiceDao.insertInvoices(item)
                        Log.e("Error", item.toString())
                    }
                } catch (ex: Exception) {
                    Log.e("Error", ex.toString())

                }

            }

            override fun shouldFetch(data: List<Invoice>?): Boolean {
                return true // Always fetch fresh data
            }

            override fun loadFromDb(): LiveData<List<Invoice>> {
                return invoiceDao.getAllInvoices()
            }

            override fun createCall(): LiveData<ApiResponse<List<Invoice>>> {
                Log.e("loadd", ApiClient.getApiService().loadInvoices().toString())
                return ApiClient.getApiService().loadInvoices()
            }
        }.asLiveData()
    }

    suspend fun addInvoice(invoice: Invoice) : Long {
        return invoiceDao.insert(invoice)
    }

    fun getInvoiceById(id: Int): LiveData<Invoice> {
        return invoiceDao.getInvoiceById(id)
    }

    suspend fun getUnsyncedInvoices(): List<Invoice> {
        return invoiceDao.getUnsyncedInvoices()
    }

    suspend fun markInvoiceAsSynced(invoice: Invoice) {
        invoiceDao.update(invoice.copy(isSynced = 1))
    }



}