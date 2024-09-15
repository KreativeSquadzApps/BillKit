package com.kreativesquadz.billkit.ui.customerManag.customerDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CustomerSharedViewModel @Inject constructor(private val repository: CustomerManagRepository) : ViewModel() {
    private val _customerId = MutableLiveData<String>()
    val customerId: LiveData<String> get() = _customerId

    private val _customer = MutableLiveData<Customer?>()
    val customer: LiveData<Customer?> get() = _customer

    fun setCustomer(id: String) {
        _customerId.value = id
        _customer.value = repository.getCustomer(id)
    }


    fun clearCustomer() {
        _customer.value = null
    }



}
