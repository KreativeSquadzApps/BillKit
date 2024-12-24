package com.kreativesquadz.hisabkitab.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kreativesquadz.hisabkitab.model.CompanyDetails
import com.kreativesquadz.hisabkitab.model.InvoicePrefixNumber

@Dao
interface CompanyDetailsDao {
    @Query("SELECT * FROM companyDetails WHERE userId = :userId")
    fun getCompanyDetails(userId: Long): LiveData<CompanyDetails>

      @Query("SELECT * FROM companyDetails WHERE userId = :userId")
    fun getCompanyDetailsById(userId: Long): CompanyDetails?

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun insertCompanyDetails(companyDetails: CompanyDetails)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(companyDetails: CompanyDetails)

    @Query("UPDATE companyDetails SET InvoicePrefix  = :invoicePrefix,InvoiceNumber = :invoiceNumber WHERE userId = :userId")
     fun updateInvoiceNumberAndPrefix(userId: Long,invoicePrefix: String ,invoiceNumber: Int)

    @Query("UPDATE companyDetails SET InvoiceNumber = :invoiceNumber WHERE userId = :userId")
    suspend fun updateInvoiceNumber(userId: Long, invoiceNumber: Int)



    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertInvoicePrefixNumber(invoicePrefixNumber: InvoicePrefixNumber): Long

      @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertInvoicePrefixNumberList(invoicePrefixNumberList: List<InvoicePrefixNumber>)

     @Query("SELECT * FROM invoice_prefix_number WHERE userId = :userId")
    fun getInvoicePrefixNumberList(userId: Long): LiveData<List<InvoicePrefixNumber>>

    @Query("SELECT * FROM invoice_prefix_number WHERE id = :id")
    fun getInvoicePrefixNumber(id: Long): InvoicePrefixNumber

    @Query("SELECT * FROM invoice_prefix_number WHERE invoicePrefix = :invoicePrefix")
    fun getInvoicePrefixNumberWithPrefix(invoicePrefix: String): InvoicePrefixNumber
    @Query("UPDATE invoice_prefix_number SET invoicePrefix = :invoicePrefix,invoiceNumber = :invoiceNumber WHERE id = :id")
    fun updateInvoiceNumberAndPrefixTable(id: Long, invoicePrefix: String, invoiceNumber: Int)

    @Query("DELETE FROM invoice_prefix_number WHERE id = :id")
    suspend fun deleteInvoicePrefixNumber(id: Long)

    @Query("DELETE FROM invoice_prefix_number WHERE userId = :userId")
     fun deleteInvoicePrefixNumberList(userId: Long)





}