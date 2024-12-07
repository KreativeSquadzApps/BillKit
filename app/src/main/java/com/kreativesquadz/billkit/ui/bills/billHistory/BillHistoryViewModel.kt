package com.kreativesquadz.billkit.ui.bills.billHistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.api.common.common.Status
import com.kreativesquadz.billkit.model.UserSession
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.LoginRepository
import com.kreativesquadz.billkit.repository.UserSessionRepository
import com.kreativesquadz.billkit.utils.PreferencesHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

    private val _invoicesList = MutableStateFlow<Resource<List<Invoice>>>(Resource.loading(null))
    val invoicesList: StateFlow<Resource<List<Invoice>>> = _invoicesList.asStateFlow()

    val _loginResponse = MutableLiveData<UserSession>()
    var loginResponse : LiveData<UserSession> = _loginResponse

//
//    private fun fetchAllInvoices() {
//        invoicess = repository.loadAllInvoices()
//    }
//
//    fun getPagedInvoicesFromDb(startDate: Long, endDate: Long) {
//        viewModelScope.launch {
//            repository.getPagedInvoicesFromDb(startDate, endDate)
//                .cachedIn(viewModelScope)
//                .collect { pagingData ->
//                    _invoices.value = pagingData
//                }
//        }
//    }
    fun fetchAllInvoices(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            try {
                repository.loadAllInvoices()
                    .asFlow()
                    .collect { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                _invoicesList.value = Resource.success(resource.data)
                                // Call getPagedInvoicesFromDb once invoices are fetched
                                getPagedInvoicesFromDb(startDate, endDate)
                            }
                            Status.ERROR -> {
                                _invoicesList.value = Resource.error(resource.message ?: "Unknown error", null)
                            }
                            Status.LOADING -> {
                                _invoicesList.value = Resource.loading(null)
                            }
                        }
                    }
            } catch (e: Exception) {
                _invoicesList.value = Resource.error("Failed to load invoices", null)
            }
        }
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