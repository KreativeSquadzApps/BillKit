package com.kreativesquadz.billkit.Dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertInvoices(invoiceList: List<Invoice>)

    @Query("SELECT * FROM invoices ")
    fun getAllInvoices(): LiveData<List<Invoice>>


    @Query("SELECT * FROM invoices WHERE invoiceDate BETWEEN :startDate AND :endDate ORDER BY invoiceDate DESC")
    fun getPagedInvoicesByDateRange(startDate: Long, endDate: Long): PagingSource<Int, Invoice>


    @Query("DELETE FROM invoice_items")
    fun deleteInvoiceItems()

    @Query("DELETE FROM invoices")
    fun deleteInvoices()
    @Query("UPDATE invoice_items SET returnedQty = :returnedQty WHERE invoiceId = :invoiceId AND itemName = :itemName")
    suspend fun updateReturnedQty(invoiceId: Long, itemName: String, returnedQty: Int)


    @Query("UPDATE invoices SET status = :status WHERE id = :invoiceId")
    suspend fun updateInvoiceStatus(status : String,  invoiceId: Int)
    @Insert
    fun insertRepo(invoice: Invoice) : Long
    @Insert
    suspend fun insert(invoice: Invoice) : Long
    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertInvoiceItem(invoiceItem: InvoiceItem)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertInvoiceItem(invoiceList: List<InvoiceItem>)

    @Update
    suspend fun update(invoice: Invoice)

    @Query("SELECT * FROM invoices WHERE id = :id")
    fun getInvoiceById(id: Int): LiveData<Invoice>

    @Query("SELECT * FROM invoices WHERE id = :id")
    fun getInvoiceByIdWithoutLiveData(id: Int): Invoice

    @Query("SELECT * FROM invoices WHERE isSynced = 0")
    suspend fun getUnsyncedInvoices(): List<Invoice>

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :id")
     fun getInvoiceItems(id: Long): List<InvoiceItem>

    @Query("SELECT * FROM invoice_items WHERE orderId = :orderId")
    fun getInvoiceItemsByOrderId(orderId: Long): List<InvoiceItem>

    @Query("SELECT * FROM invoices WHERE customerId = :customerId AND creditAmount > 0  ORDER BY invoiceDate DESC")
    fun getAllInvoicesFlow(customerId : Long): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE invoiceDate BETWEEN :startDate AND :endDate ORDER BY invoiceDate DESC")
    suspend fun getInvoicesByDate(startDate: Long,  endDate: Long): List<Invoice>

}