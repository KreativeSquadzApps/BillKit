package com.kreativesquadz.hisabkitab.Dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kreativesquadz.hisabkitab.model.Invoice
import com.kreativesquadz.hisabkitab.model.InvoiceItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertInvoices(invoiceList: List<Invoice>)

    @Query("SELECT * FROM invoices")
    fun getAllInvoices(): LiveData<List<Invoice>>


    @Query("SELECT * FROM invoices WHERE invoiceDate BETWEEN :startDate AND :endDate ORDER BY invoiceDate DESC")
    fun getPagedInvoicesByDateRange(startDate: Long, endDate: Long): PagingSource<Int, Invoice>


    @Query("DELETE FROM invoice_items")
    fun deleteInvoiceItems()

    @Delete
    suspend fun deleteInvoiceItem(invoiceItem: InvoiceItem)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateInvoiceItem(invoiceItem: InvoiceItem)

    @Query("DELETE FROM invoices")
    fun deleteInvoices()

    @Query("UPDATE invoice_items SET returnedQty = :returnedQty WHERE invoiceId = :invoiceId AND itemName = :itemName")
    suspend fun updateReturnedQty(invoiceId: Long, itemName: String, returnedQty: Int)

    @Query("UPDATE invoices SET status = :status WHERE invoiceId = :invoiceId")
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

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateInvoice(invoice: Invoice): Int


    @Query("SELECT * FROM invoices WHERE id = :id")
     fun getInvoiceById(id: Int): LiveData<Invoice>

    @Query("SELECT * FROM invoices WHERE invoiceId = :invoiceId")
    fun getInvoiceByInvoiceId(invoiceId: Int): LiveData<Invoice>

    @Query("SELECT * FROM invoices WHERE id = :id")
    fun getInvoiceByIdWithoutLiveData(id: Int): Invoice

    @Query("SELECT * FROM invoices WHERE invoiceNumber = :invoiceNumber COLLATE NOCASE")
    fun getInvoiceByInvoiceNumberWithoutLiveData(invoiceNumber: String): Invoice

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