package com.kreativesquadz.hisabkitab.ui.bills.creditNote

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.api.common.common.Resource
import com.kreativesquadz.hisabkitab.model.CreditNote
import com.kreativesquadz.hisabkitab.repository.CreditNoteRepository
import com.kreativesquadz.hisabkitab.utils.PreferencesHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreditNoteViewModel @Inject constructor(val creditNoteRepository: CreditNoteRepository,
                                              val preferencesHelper: PreferencesHelper
) : ViewModel() {
    lateinit var creditNoteList : LiveData<Resource<List<CreditNote>>>
    var _creditNotes = MutableStateFlow<PagingData<CreditNote>>(PagingData.empty())
    val creditNotes: StateFlow<PagingData<CreditNote>> = _creditNotes
    private var originalCreditNotes: PagingData<CreditNote>? = null
    init {
        fetchAllCreditNotes()
    }
    private fun fetchAllCreditNotes(){
        creditNoteList = creditNoteRepository.loadAllCreditNote(Config.userId)
    }

    fun getPagedCreditNotesFromDb(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            creditNoteRepository.getPagedCreditNoteFromDb(startDate, endDate)
                .cachedIn(viewModelScope)
                .collect { pagingData ->
                    originalCreditNotes = pagingData
                    _creditNotes.value  = pagingData
                }
        }
    }
    fun filterCreditNotes(selected: String?) {
        originalCreditNotes?.let { notes ->
            _creditNotes.value = if (selected == "All") {
                notes // No need to assert non-null here
            } else {
                notes.filter { creditNote -> creditNote.status == selected }
            }
        }
    }

    fun saveSelectedDate(timestamp: Long) {
        preferencesHelper.saveSelectedDateCreditNote(timestamp)
    }
    fun getSelectedDate(): Long {
        return preferencesHelper.getSelectedDateCreditNote()
    }

    
}