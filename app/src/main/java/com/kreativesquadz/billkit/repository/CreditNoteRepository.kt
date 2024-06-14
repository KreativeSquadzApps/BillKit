package com.kreativesquadz.billkit.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kreativesquadz.billkit.Dao.CreditNoteDao
import com.kreativesquadz.billkit.Database.AppDatabase
import com.kreativesquadz.billkit.model.CreditNote
import javax.inject.Inject

class CreditNoteRepository @Inject constructor(db: AppDatabase) {
    private val creditNoteDao : CreditNoteDao = db.creditNoteDao()
    suspend fun addCreditNote(creditNote: CreditNote) {
        creditNoteDao.insertCreditNote(creditNote)
        val statusLiveData = MutableLiveData<Boolean>()
        statusLiveData.value = true
    }
    suspend fun updateCreditNote(creditNote: CreditNote) {
        creditNoteDao.updateCreditNote(creditNote)
        val statusLiveData = MutableLiveData<Boolean>()
        statusLiveData.value = true
    }



    suspend fun getCreditNoteByInvoiceId(creditNote: CreditNote) : CreditNote?  {
       return creditNoteDao.getCreditNoteByInvoiceId(creditNote.invoiceId)
    }
    suspend fun getUnsyncedCreditNote(): List<CreditNote> {
        return creditNoteDao.getUnsyncedCreditNote()
    }

    suspend fun markCreditNoteAsSynced(creditNote: CreditNote) {
        creditNoteDao.updateCreditNote(creditNote)
    }

    suspend fun updateInvoiceStatus(invoiceId: Int) {
        creditNoteDao.updateInvoiceStatus(invoiceId)
    }

    suspend fun redeemCreditNoteById(id: Int?) {
        creditNoteDao.updateCreditNoteStatus(id, "Redeemed")
    }


}