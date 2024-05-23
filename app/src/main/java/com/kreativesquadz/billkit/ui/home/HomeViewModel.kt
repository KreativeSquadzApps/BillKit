package com.kreativesquadz.billkit.ui.home

import androidx.lifecycle.ViewModel
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(var billHistoryRepository: BillHistoryRepository, var customerManagRepository: CustomerManagRepository) : ViewModel(){
    fun addInvoice(invoice: Invoice){
        //billHistoryRepository.addInvoice(invoice)
    }
}