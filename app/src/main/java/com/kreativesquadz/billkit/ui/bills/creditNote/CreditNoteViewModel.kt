package com.kreativesquadz.billkit.ui.bills.creditNote

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.CreditNote
import com.kreativesquadz.billkit.model.Product
import com.kreativesquadz.billkit.repository.CreditNoteRepository
import com.kreativesquadz.billkit.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreditNoteViewModel @Inject constructor(val creditNoteRepository: CreditNoteRepository) : ViewModel() {
    lateinit var creditNoteList : LiveData<Resource<List<CreditNote>>>
    fun getCreditNotes(){
        creditNoteList = creditNoteRepository.loadAllCreditNote(Config.userId)
    }
    
}