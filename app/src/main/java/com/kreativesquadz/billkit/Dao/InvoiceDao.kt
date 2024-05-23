package com.kreativesquadz.billkit.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kreativesquadz.billkit.model.Invoice

@Dao
interface InvoiceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertInvoices(invoiceList: List<Invoice>)

     @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertInvoice(invoiceList: Invoice)

    @Query("SELECT * FROM invoices")
    fun getAllInvoices(): LiveData<List<Invoice>>

    @Query("DELETE FROM invoices")
    fun deleteInvoices()

    @Query("DELETE FROM invoice_items")
    fun deleteInvoiceItems()



}