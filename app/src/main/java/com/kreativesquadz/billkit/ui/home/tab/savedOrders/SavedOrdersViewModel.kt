package com.kreativesquadz.billkit.ui.home.tab.savedOrders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.api.ApiStatus
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Product
import com.kreativesquadz.billkit.model.SavedOrder
import com.kreativesquadz.billkit.repository.SavedOrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedOrdersViewModel @Inject constructor(val repository: SavedOrderRepository) : ViewModel() {

    private var _savedOrder = MutableLiveData<List<SavedOrder>>()
    val savedOrder: LiveData<List<SavedOrder>> get() = _savedOrder


//    fun saveOrder(savedOrder: SavedOrder) {
//        viewModelScope.launch {
//            repository.saveOrder(savedOrder)
//        }
//    }

    fun getSavedOrders() {
        viewModelScope.launch {
            _savedOrder.value = repository.getSavedOrders()
            // Update UI with saved orders
        }
    }
    
}