package com.kreativesquadz.billkit.ui.bottomSheet.editItemBottomSheet

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditItemBottomSheetViewModel @Inject constructor(val repository: CustomerManagRepository) : ViewModel() {
    lateinit var customer : LiveData<Resource<List<Customer>>>
    fun getCustomers(){
        customer = repository.loadAllCustomers()
    }

}