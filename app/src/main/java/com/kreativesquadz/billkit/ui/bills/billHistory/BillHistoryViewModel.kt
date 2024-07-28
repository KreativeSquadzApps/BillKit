package com.kreativesquadz.billkit.ui.bills.billHistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.LoginResponse
import com.kreativesquadz.billkit.model.UserSession
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import com.kreativesquadz.billkit.repository.LoginRepository
import com.kreativesquadz.billkit.repository.UserSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class BillHistoryViewModel @Inject constructor(val repository: BillHistoryRepository,
                                               val customerRepository: CustomerManagRepository,
                                               val loginRepository: LoginRepository,
                                               val userSessionRepository: UserSessionRepository
) : ViewModel() {

    lateinit var invoices : LiveData<Resource<List<Invoice>>>
    lateinit var customers : LiveData<Resource<List<Customer>>>
    val _loginResponse = MutableLiveData<UserSession>()
    var loginResponse : LiveData<UserSession> = _loginResponse
    //val userSession: LiveData<UserSession> = userSessionRepository.userSession
    fun getAllInvoices(): LiveData<Resource<List<Invoice>>> {
        customers = customerRepository.loadAllCustomers()
        invoices = repository.loadAllInvoices()
        return invoices
    }
    fun getSesssion() {
            _loginResponse.value = loginRepository.getUserSessions()
    }



}