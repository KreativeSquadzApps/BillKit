package com.kreativesquadz.billkit.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kreativesquadz.billkit.Dao.CreditNoteDao
import com.kreativesquadz.billkit.Dao.InventoryDao
import com.kreativesquadz.billkit.Dao.InvoiceDao
import com.kreativesquadz.billkit.Database.AppDatabase
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.api.ApiResponse
import com.kreativesquadz.billkit.api.common.NetworkBoundResource
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import javax.inject.Inject

class BillHistoryRepository @Inject constructor(private val db: AppDatabase) {
    private val invoiceDao : InvoiceDao = db.invoiceDao()
    private val inventoryDao : InventoryDao = db.inventoryDao()

    fun loadAllInvoices(): LiveData<Resource<List<Invoice>>> {
        return object : NetworkBoundResource<List<Invoice>, List<Invoice>>() {
            override fun saveCallResult(item: List<Invoice>) {
                try {
                    Log.e("invoice", item.toString())
                    db.runInTransaction {
                        // Clear existing data
                        invoiceDao.deleteInvoices()
                        invoiceDao.insertInvoices(item)
                        item.forEach{
                            invoiceDao.insertInvoiceItem(it.invoiceItems!!)
                        }


                        Log.e("Error", item.toString())
                    }
                } catch (ex: Exception) {
                    Log.e("Errortry", ex.toString())

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


    suspend fun updateInvoiceItem(invoiceId: Long, itemName: String, returnedQty: Int) {
        invoiceDao.updateReturnedQty(invoiceId,itemName, returnedQty)
    }
    suspend fun insertInvoiceWithItems(invoice: Invoice, items: List<InvoiceItem>) : Long{
        try {
            // Insert the invoice
            val invoiceId = invoiceDao.insert(invoice)

            // Insert each invoice item
            items.forEach { item ->
                invoiceDao.insertInvoiceItem(item.copy(invoiceId = invoiceId))
                val productName = item.itemName.split(" ")[0]
                inventoryDao.decrementProductStock(productName, item.quantity)
            }
            return invoiceId
        } catch (e: Exception) {
            throw e
        }

    }


    suspend fun getInvoiceItems(id: Long): List<InvoiceItem> {
        return invoiceDao.getInvoiceItems(id)
    }

}