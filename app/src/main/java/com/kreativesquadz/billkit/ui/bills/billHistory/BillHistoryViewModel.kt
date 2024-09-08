package com.kreativesquadz.billkit.ui.bills.billHistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.UserSession
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.LoginRepository
import com.kreativesquadz.billkit.repository.UserSessionRepository
import com.kreativesquadz.billkit.utils.PreferencesHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillHistoryViewModel @Inject constructor(val repository: BillHistoryRepository,
                                               val loginRepository: LoginRepository,
                                               val preferencesHelper: PreferencesHelper,
                                               val userSessionRepository: UserSessionRepository
) : ViewModel() {
    var _invoices = MutableStateFlow<PagingData<Invoice>>(PagingData.empty())
    val invoices: StateFlow<PagingData<Invoice>> = _invoices
    lateinit var invoicess : LiveData<Resource<List<Invoice>>>
    val _loginResponse = MutableLiveData<UserSession>()
    var loginResponse : LiveData<UserSession> = _loginResponse

    init {
         fetchAllInvoices()
    }

    private fun fetchAllInvoices() {
        invoicess = repository.loadAllInvoices()
    }

    fun getPagedInvoicesFromDb(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            repository.getPagedInvoicesFromDb(startDate, endDate)
                .cachedIn(viewModelScope)
                .collect { pagingData ->
                    _invoices.value = pagingData
                }
        }
    }
    fun saveSelectedDate(timestamp: Long) {
        preferencesHelper.saveSelectedDate(timestamp)
    }
    fun getSelectedDate(): Long {
        return preferencesHelper.getSelectedDate()
    }


    fun getSesssion() {
     _loginResponse.value = loginRepository.getUserSessions()
    }

}