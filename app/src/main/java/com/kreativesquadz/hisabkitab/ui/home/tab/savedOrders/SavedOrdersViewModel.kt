package com.kreativesquadz.hisabkitab.ui.home.tab.savedOrders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.api.common.common.Resource
import com.kreativesquadz.hisabkitab.model.SavedOrder
import com.kreativesquadz.hisabkitab.repository.SavedOrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedOrdersViewModel @Inject constructor(val repository: SavedOrderRepository) : ViewModel() {


    private val _savedOrders = MediatorLiveData<Resource<List<SavedOrder>>>()
    val savedOrders: LiveData<Resource<List<SavedOrder>>> get() = _savedOrders


//    fun saveOrder(savedOrder: SavedOrder) {
//        viewModelScope.launch {
//            repository.saveOrder(savedOrder)
//        }
//    }

    fun loadSavedOrders(  ) {
        val source = repository.loadSavedOrders(Config.userId)
        _savedOrders.addSource(source) { resource ->
            viewModelScope.launch {
                val savedOrders = resource.data?.map { orderEntity ->
                    val invoiceItems = repository.getInvoiceItemsByOrderId(orderEntity.orderId)
                    SavedOrder(
                        orderId = orderEntity.orderId,
                        orderName = orderEntity.orderName,
                        totalAmount = orderEntity.totalAmount,
                        date = orderEntity.date,
                        items = invoiceItems
                    )
                } ?: emptyList()
                _savedOrders.postValue(Resource.success(savedOrders))
            }
        }
    }

    fun deleteSavedOrder(orderId: Long){
        viewModelScope.launch {
            repository.deleteSavedOrder(orderId)
        }
    }

    
}