package com.kreativesquadz.billkit.ui.bills.billHistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import javax.inject.Inject

@HiltViewModel
class BillHistoryViewModel @Inject constructor(val repository: BillHistoryRepository, val customerRepository: CustomerManagRepository) : ViewModel() {
    lateinit var invoices : LiveData<Resource<List<Invoice>>>
    lateinit var customers : LiveData<Resource<List<Customer>>>
    fun getAllInvoices(): LiveData<Resource<List<Invoice>>> {
        customers = customerRepository.loadAllCustomers()
        invoices = repository.loadAllInvoices()
        return invoices
    }



}