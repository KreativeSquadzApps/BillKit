package com.kreativesquadz.billkit.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.model.Product
import com.kreativesquadz.billkit.model.SavedOrder
import com.kreativesquadz.billkit.model.UserSetting
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import com.kreativesquadz.billkit.repository.InventoryRepository
import com.kreativesquadz.billkit.repository.SavedOrderRepository
import com.kreativesquadz.billkit.repository.UserSettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(val inventoryRepository: InventoryRepository,
                                        val userSettingRepository: UserSettingRepository,
                                        val savedOrderRepository: SavedOrderRepository,
                                        val billHistoryRepository: BillHistoryRepository)  : ViewModel(){

    lateinit var userSetting : LiveData<UserSetting>


    fun addInvoice(invoice: Invoice){
        //billHistoryRepository.addInvoice(invoice)
    }
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