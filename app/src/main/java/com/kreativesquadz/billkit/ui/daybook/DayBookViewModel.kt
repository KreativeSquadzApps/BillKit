package com.kreativesquadz.billkit.ui.daybook

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import com.kreativesquadz.billkit.repository.LoginRepository
import com.kreativesquadz.billkit.utils.PreferencesHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DayBookViewModel @Inject constructor(val repository: BillHistoryRepository,
                                           val loginRepository: LoginRepository,
                                           val preferencesHelper: PreferencesHelper,
                                           val customerManagRepository: CustomerManagRepository
) : ViewModel() {

    private var _invoices : MutableLiveData<List<Invoice>> = MutableLiveData()
    val invoices : LiveData<List<Invoice>> = _invoices



    fun getInvoices(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            _invoices.value = repository.getInvoicesByDate(startDate, endDate)
        }
    }

    fun saveSelectedDate(timestamp: Long) {
        preferencesHelper.saveSelectedDate(timestamp)
    }
    fun getSelectedDate(): Long {
        return preferencesHelper.getSelectedDate()
    }

    fun getCustomerById(id: String) : Customer {
        return customerManagRepository.getCustomer(id)
    }
}