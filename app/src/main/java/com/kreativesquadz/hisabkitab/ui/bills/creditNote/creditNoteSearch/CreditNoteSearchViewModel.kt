package com.kreativesquadz.hisabkitab.ui.bills.creditNote.creditNoteSearch

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.api.common.common.Resource
import com.kreativesquadz.hisabkitab.model.CreditNote
import com.kreativesquadz.hisabkitab.repository.CreditNoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreditNoteSearchViewModel @Inject constructor(val repository: CreditNoteRepository) : ViewModel() {
    lateinit var creditNoteList : LiveData<Resource<List<CreditNote>>>
    fun getcreditNoteList(){
        creditNoteList = repository.loadAllCreditNote(Config.userId)
    }
}