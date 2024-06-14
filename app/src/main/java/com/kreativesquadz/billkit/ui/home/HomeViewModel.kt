package com.kreativesquadz.billkit.ui.home

import androidx.lifecycle.ViewModel
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.Product
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import com.kreativesquadz.billkit.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(val inventoryRepository: InventoryRepository) : ViewModel(){
    fun addInvoice(invoice: Invoice){
        //billHistoryRepository.addInvoice(invoice)
    }
    fun getProductDetailByBarcode(barcode: String): Product {
        return  inventoryRepository.getProductByBarcode(barcode)
    }
}