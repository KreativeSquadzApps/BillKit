package com.kreativesquadz.billkit.ui.bills.billHistory

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BillHistoryViewModel @Inject constructor(val repository: BillHistoryRepository) : ViewModel() {
    lateinit var invoices : LiveData<Resource<List<Invoice>>>

    fun getAllInvoices(): LiveData<Resource<List<Invoice>>> {
        invoices = repository.loadAllInvoices()
        return invoices
    }


}