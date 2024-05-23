package com.kreativesquadz.billkit.repository

import androidx.lifecycle.LiveData
import com.kreativesquadz.billkit.Dao.InvoiceDao
import com.kreativesquadz.billkit.Database.AppDatabase
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.api.ApiResponse
import com.kreativesquadz.billkit.api.common.NetworkBoundResource
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Invoice
import timber.log.Timber
import java.io.IOException
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

                        Timber.d("loadAllInvoices() All invoices saved")
                    }
                } catch (ex: Exception) {
                    Timber.d(" Error at saveCallResult ${ex}")

                }
            }

            override fun shouldFetch(data: List<Invoice>?): Boolean {
                return true // Always fetch fresh data
            }

            override fun loadFromDb(): LiveData<List<Invoice>> {
                return invoiceDao.getAllInvoices()
            }

            override fun createCall(): LiveData<ApiResponse<List<Invoice>>> {
                return ApiClient.getApiService().loadInvoices()
            }
        }.asLiveData()
    }

    // Method to generate an invoice
    suspend fun generateInvoice(invoice: Invoice): Boolean {
        try {

            Timber.tag("Request").e(invoice.toString())
            val response = ApiClient.getApiService().createInvoice(invoice)
            return response.isSuccessful
        } catch (e: IOException) {
            e.printStackTrace()
            Timber.tag("Error").e("Network error: " + e.message)
        }
        return false
    }
}