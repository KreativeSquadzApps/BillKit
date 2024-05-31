package com.kreativesquadz.billkit.ui.bills.billHistory.invoice

import androidx.lifecycle.ViewModel
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(val customerManagRepository: CustomerManagRepository) : ViewModel() {

  fun getCustomerById(id: String) : Customer {
    return customerManagRepository.getCustomer(id)
  }


}