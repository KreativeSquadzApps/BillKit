package com.kreativesquadz.hisabkitab.ui.bottomSheet.customerBottomSheet

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kreativesquadz.hisabkitab.api.common.common.Resource
import com.kreativesquadz.hisabkitab.model.Customer
import com.kreativesquadz.hisabkitab.repository.CustomerManagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CustomerAddBottomSheetViewModel @Inject constructor(val repository: CustomerManagRepository) : ViewModel() {
    lateinit var customer : LiveData<Resource<List<Customer>>>
    fun getCustomers(){
        customer = repository.loadAllCustomers()
    }
}