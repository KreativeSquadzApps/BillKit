package com.kreativesquadz.hisabkitab.ui.home.tab.sale

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.api.common.common.Resource
import com.kreativesquadz.hisabkitab.model.Category
import com.kreativesquadz.hisabkitab.model.Product
import com.kreativesquadz.hisabkitab.repository.InventoryRepository
import com.kreativesquadz.hisabkitab.repository.UserSettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SaleViewModel @Inject constructor(val inventoryRepository: InventoryRepository,
                                        val userSettingRepository: UserSettingRepository
) : ViewModel() {
    lateinit var products : LiveData<Resource<List<Product>>>
    lateinit var category : LiveData<Resource<List<Category>>>
    val filteredProducts: MutableLiveData<List<Product>> = MutableLiveData()
    val selectedCategory: MutableLiveData<String?> = MutableLiveData(null)

    fun getProducts(){
        products = inventoryRepository.loadAllProduct(Config.userId)
    }


    fun getCategories(){
        category = inventoryRepository.loadAllCategory(Config.userId)
    }

    fun filterProducts(productList: List<Product>, category: String?) {
        val filteredList = if (category.isNullOrEmpty()) {
            productList
        }
        else if (category == "All") {
            productList
        }
        else {
            productList.filter { it.category == category }
        }
        filteredProducts.value = filteredList
    }

}
