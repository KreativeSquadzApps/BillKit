package com.kreativesquadz.billkit.ui.inventory.tab.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Category
import com.kreativesquadz.billkit.model.Product
import com.kreativesquadz.billkit.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(val inventoryRepository: InventoryRepository) : ViewModel() {
    lateinit var products : LiveData<Resource<List<Product>>>
    fun getProducts(){
        products = inventoryRepository.loadAllProduct(Config.userId)
    }
}