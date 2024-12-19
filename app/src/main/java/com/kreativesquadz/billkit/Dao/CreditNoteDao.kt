package com.kreativesquadz.billkit.Dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kreativesquadz.billkit.model.CreditNote
import com.kreativesquadz.billkit.model.Invoice

@Dao
interface CreditNoteDao {
    @Query("SELECT * FROM credit_notes WHERE userId = :userId")
    fun getCreditNotesByUser(userId: Long): LiveData<List<CreditNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditNote(creditNote: CreditNote): Long

    @Update
    suspend fun update(creditNote: CreditNote)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCreditNoteList(creditNote: List<CreditNote>)

    @Query("SELECT * FROM credit_notes WHERE isSynced = 0")
    suspend fun getUnsyncedCreditNote(): List<CreditNote>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCreditNote(creditNote: CreditNote)


    @Query("SELECT * FROM credit_notes WHERE invoiceId = :invoiceId LIMIT 1")
    suspend fun getCreditNoteByInvoiceId(invoiceId: Long): CreditNote?

    @Query("SELECT * FROM credit_notes WHERE id = :id LIMIT 1")
    suspend fun getCreditNoteById(id: Long): CreditNote?

    @Query("DELETE FROM credit_notes")
    fun deleteCreditNoteList()

    @Query("UPDATE credit_notes SET status = :status WHERE id = :id")
    suspend fun updateCreditNoteStatus(id: Int?, status: String)

    @Query("UPDATE invoices SET status = 'Returned' WHERE invoiceId = :invoiceId")
    suspend fun updateInvoiceStatus(invoiceId: Int)

    @Query("SELECT * FROM credit_notes WHERE dateTime BETWEEN :startDate AND :endDate ORDER BY dateTime DESC")
    fun getPagedCreditNoteByDateRange(startDate: Long, endDate: Long): PagingSource<Int, CreditNote>


}
