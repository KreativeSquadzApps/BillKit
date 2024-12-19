package com.kreativesquadz.billkit.ui.bills.creditNote.creditNoteSearch

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.CreditNote
import com.kreativesquadz.billkit.repository.CreditNoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreditNoteSearchViewModel @Inject constructor(val repository: CreditNoteRepository) : ViewModel() {
    lateinit var creditNoteList : LiveData<Resource<List<CreditNote>>>
    fun getcreditNoteList(){
        creditNoteList = repository.loadAllCreditNote(Config.userId)
    }
}