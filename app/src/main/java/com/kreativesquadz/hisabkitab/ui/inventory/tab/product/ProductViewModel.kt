package com.kreativesquadz.hisabkitab.ui.inventory.tab.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.api.common.common.Resource
import com.kreativesquadz.hisabkitab.model.Category
import com.kreativesquadz.hisabkitab.model.Product
import com.kreativesquadz.hisabkitab.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(val inventoryRepository: InventoryRepository) : ViewModel() {
    lateinit var products : LiveData<Resource<List<Product>>>
    lateinit var categories : LiveData<Resource<List<Category>>>
    fun getCategories(){
        categories = inventoryRepository.loadAllCategory(Config.userId)
    }
    fun getProducts(){
        products = inventoryRepository.loadAllProduct(Config.userId)
    }
}