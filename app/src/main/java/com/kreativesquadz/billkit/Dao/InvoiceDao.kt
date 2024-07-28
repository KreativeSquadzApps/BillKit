package com.kreativesquadz.billkit.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem

@Dao
interface InvoiceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertInvoices(invoiceList: List<Invoice>)

    @Query("SELECT * FROM invoices")
    fun getAllInvoices(): LiveData<List<Invoice>>

    @Query("DELETE FROM invoice_items")
    fun deleteInvoiceItems()

    @Query("DELETE FROM invoices")
    fun deleteInvoices()
    @Query("UPDATE invoice_items SET returnedQty = :returnedQty WHERE invoiceId = :invoiceId AND itemName = :itemName")
    suspend fun updateReturnedQty(invoiceId: Long, itemName: String, returnedQty: Int)
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

    @Query("SELECT * FROM invoices WHERE isSynced = 0")
    suspend fun getUnsyncedInvoices(): List<Invoice>

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :id")
     fun getInvoiceItems(id: Long): List<InvoiceItem>
}