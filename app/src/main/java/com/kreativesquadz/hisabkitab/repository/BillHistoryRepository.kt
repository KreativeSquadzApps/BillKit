package com.kreativesquadz.hisabkitab.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.kreativesquadz.hisabkitab.Dao.InventoryDao
import com.kreativesquadz.hisabkitab.Dao.InvoiceDao
import com.kreativesquadz.hisabkitab.Database.AppDatabase
import com.kreativesquadz.hisabkitab.api.ApiClient
import com.kreativesquadz.hisabkitab.api.ApiResponse
import com.kreativesquadz.hisabkitab.api.common.NetworkBoundResource
import com.kreativesquadz.hisabkitab.api.common.common.Resource
import com.kreativesquadz.hisabkitab.model.Invoice
import com.kreativesquadz.hisabkitab.model.InvoiceItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BillHistoryRepository @Inject constructor(private val db: AppDatabase) {
    private val invoiceDao : InvoiceDao = db.invoiceDao()
    private val inventoryDao : InventoryDao = db.inventoryDao()

    fun loadAllInvoices(): LiveData<Resource<List<Invoice>>> {
        return object : NetworkBoundResource<List<Invoice>, List<Invoice>>() {
            override fun saveCallResult(item: List<Invoice>) {
                try {
                    db.runInTransaction {
                        // Clear existing data
                        invoiceDao.deleteInvoices()
                        invoiceDao.insertInvoices(item)
                        invoiceDao.deleteInvoiceItems()
                        item.forEach{
                            Log.e("itemppppp",it.toString())
                            invoiceDao.insertInvoiceItem(it.invoiceItems!!)
                        }
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
                return ApiClient.getApiService().loadInvoices()
            }
        }.asLiveData()
    }

    fun getInvoiceById(id: Int): LiveData<Invoice> {
        return invoiceDao.getInvoiceById(id)
    }

    fun getInvoiceByInvoiceId(invoiceId: Int): LiveData<Invoice> {
        return invoiceDao.getInvoiceByInvoiceId(invoiceId)
    }

    fun getInvoiceByIdWithoutLiveData(id: Int): Invoice {
        return invoiceDao.getInvoiceByIdWithoutLiveData(id)
    }

    fun getInvoiceByInvoiceNumberWithoutLiveData(invoiceNumber: String): Invoice {
        return invoiceDao.getInvoiceByInvoiceNumberWithoutLiveData(invoiceNumber)
    }

    fun getPagedInvoicesFromDb(startDate: Long, endDate: Long): Flow<PagingData<Invoice>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20, // Define the size of each page
                enablePlaceholders = false
            ),
            pagingSourceFactory = { invoiceDao.getPagedInvoicesByDateRange(startDate, endDate) }
        ).flow
    }

    suspend fun updateInvoiceStatus( status: String, invoiceId: Int) {
        invoiceDao.updateInvoiceStatus( status, invoiceId)
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
            invoiceDao.insert(invoice)



            Log.e("iteeeeemmmkk",invoice.toString())

            // Insert each invoice item
            items.forEach { item ->
                Log.e("iteeeeemmmkk",item.toString())
                invoiceDao.insertInvoiceItem(item.copy(invoiceId = invoice.invoiceId.toLong()))
                val productName = item.itemName.split("(")[0].trim()
                inventoryDao.decrementProductStock(productName, item.quantity)
            }
            return invoice.invoiceId.toLong()
        } catch (e: Exception) {
            throw e
        }

    }

    suspend fun updateInvoiceWithItems(invoice: Invoice, items: List<InvoiceItem>) : Boolean {
        val updated: Boolean
        try {
            val invoiceIdNew = invoice.invoiceId
            val rowsUpdated = invoiceDao.updateInvoice(invoice.copy(invoiceId=invoiceIdNew))
            if (rowsUpdated == 0) {
                updated = false
            } else {
                updated = true
            }
            // Get the existing items for the invoice
            val existingItems = invoiceDao.getInvoiceItems(invoiceIdNew.toLong())
            Log.e("existingItems",existingItems.toString())
            Log.e("existingNew",items.toString())

            existingItems.forEach { existingItem ->
                if (items.none { newItem -> newItem.id == existingItem.id}) {
                    // Restore stock for the removed item
                    val productName = existingItem.itemName.split("(")[0].trim()
                    inventoryDao.incrementProductStock(productName, existingItem.quantity)
                    // Delete the removed item from the database
                    invoiceDao.deleteInvoiceItem(existingItem)
                }
            }

            // Update or insert the remaining items
            items.forEach { item ->
               // invoiceDao.updateInvoiceItem(item)
                Log.e("item", item.toString())

                if (item.id == 0L) {
                    // New item, insert it
                    invoiceDao.insertInvoiceItem(item.copy(invoiceId = invoiceIdNew.toLong()))
                }
                else {
                    // Existing item, update it
                    invoiceDao.updateInvoiceItem(item)

                    // Adjust inventory based on quantity change
                    val existingItem = existingItems.find { it.id == item.id }
                    if (existingItem != null) {
                        val quantityDifference = item.quantity - existingItem.quantity
                        val productName = item.itemName.split("(")[0].trim()
                        if (quantityDifference > 0) {
                            inventoryDao.decrementProductStock(productName, quantityDifference)
                        } else if (quantityDifference < 0) {
                            inventoryDao.incrementProductStock(productName, -quantityDifference)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            throw e
        }
        return updated
    }


    fun getInvoiceItems(id: Long): List<InvoiceItem> {
        return invoiceDao.getInvoiceItems(id)
    }


    fun getAllInvoicesFlow(customerId: Long): Flow<List<Invoice>> {
        return invoiceDao.getAllInvoicesFlow(customerId)
    }

    suspend fun updateInvoice(invoice: Invoice) {
        invoiceDao.update(invoice)
    }

    suspend fun getInvoicesByDate(startDate: Long, endDate: Long) : List<Invoice>{
       return invoiceDao.getInvoicesByDate(startDate, endDate)
    }

}