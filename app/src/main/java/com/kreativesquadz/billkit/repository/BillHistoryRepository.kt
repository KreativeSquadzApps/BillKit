package com.kreativesquadz.billkit.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
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
                        item.forEach{
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

    fun getInvoiceByIdWithoutLiveData(id: Int): Invoice {
        return invoiceDao.getInvoiceByIdWithoutLiveData(id)
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
            val invoiceId = invoiceDao.insert(invoice)

            // Insert each invoice item
            items.forEach { item ->
                invoiceDao.insertInvoiceItem(item.copy(invoiceId = invoiceId))
                val productName = item.itemName.split("(")[0]
                inventoryDao.decrementProductStock(productName, item.quantity)
            }
            return invoiceId
        } catch (e: Exception) {
            throw e
        }

    }

    suspend fun updateInvoiceWithItems(invoice: Invoice, items: List<InvoiceItem>,invoiceId : Long) : Boolean {
        val updated: Boolean
        try {
            val rowsUpdated = invoiceDao.updateInvoice(invoice.copy(id = invoiceId))
            if (rowsUpdated == 0) {
                updated = false
            } else {
                updated = true
            }
            // Get the existing items for the invoice
            val existingItems = invoiceDao.getInvoiceItems(invoice.id)

            // Remove items that are no longer in the updated list
            existingItems.forEach { existingItem ->
                if (items.none { it.id == existingItem.id }) {
                    // If item is removed, restore the stock for the product
                    val productName = existingItem.itemName.split("(")[0].trim()
                    inventoryDao.incrementProductStock(productName, existingItem.quantity)
                    invoiceDao.deleteInvoiceItem(existingItem)
                }
            }

            // Update or insert the remaining items
            items.forEach { item ->
                invoiceDao.updateInvoiceItem(item)

                if (item.id == 0L) {
                    // New item, insert it
                    invoiceDao.insertInvoiceItem(item.copy(invoiceId = invoice.id))
                } else {
                    // Existing item, update it
                    //invoiceDao.updateInvoiceItem(item)

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