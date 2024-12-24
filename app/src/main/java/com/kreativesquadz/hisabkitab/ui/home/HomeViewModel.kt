package com.kreativesquadz.hisabkitab.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.api.common.common.Resource
import com.kreativesquadz.hisabkitab.model.Invoice
import com.kreativesquadz.hisabkitab.model.InvoiceItem
import com.kreativesquadz.hisabkitab.model.Product
import com.kreativesquadz.hisabkitab.model.SavedOrder
import com.kreativesquadz.hisabkitab.model.settings.UserSetting
import com.kreativesquadz.hisabkitab.repository.BillHistoryRepository
import com.kreativesquadz.hisabkitab.repository.InventoryRepository
import com.kreativesquadz.hisabkitab.repository.SavedOrderRepository
import com.kreativesquadz.hisabkitab.repository.UserSettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(val inventoryRepository: InventoryRepository,
                                        val userSettingRepository: UserSettingRepository,
                                        val savedOrderRepository: SavedOrderRepository,
                                        val billHistoryRepository: BillHistoryRepository)  : ViewModel(){

    lateinit var userSetting : LiveData<UserSetting>


    fun getProductDetailByBarcode(barcode: String): Product {
        return  inventoryRepository.getProductByBarcode(barcode)
    }

    fun getUserSettings(): LiveData<UserSetting> {
        userSetting = userSettingRepository.getUserSetting(Config.userId)
        return userSetting
    }

    fun getInvoiceHistory(): LiveData<Resource<List<Invoice>>> {
      return  billHistoryRepository.loadAllInvoices()
    }


    fun saveOrder(savedOrder: SavedOrder,item: List<InvoiceItem>) {
        viewModelScope.launch {
            savedOrderRepository.saveOrder(savedOrder,item)
        }
    }

}