package com.kreativesquadz.hisabkitab.ui.daybook

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.hisabkitab.model.Customer
import com.kreativesquadz.hisabkitab.model.Invoice
import com.kreativesquadz.hisabkitab.repository.BillHistoryRepository
import com.kreativesquadz.hisabkitab.repository.CustomerManagRepository
import com.kreativesquadz.hisabkitab.repository.LoginRepository
import com.kreativesquadz.hisabkitab.utils.PreferencesHelper
import dagger.hilt.android.lifecycle.HiltViewModel
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