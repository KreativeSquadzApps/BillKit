package com.kreativesquadz.billkit.ui.customerManag.createCustomer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateCustomerViewModel @Inject constructor(val repository: CustomerManagRepository) : ViewModel() {
    private var _customerStatus = MutableLiveData<Boolean>()
    val customerStatus: LiveData<Boolean> get() = _customerStatus

    fun addCustomerObj(customer: Customer) {
        viewModelScope.launch {
            _customerStatus.value = repository.addCustomer(customer).value
        }
    }



}