package com.kreativesquadz.billkit.ui.home.tab.sale

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Category
import com.kreativesquadz.billkit.model.Product
import com.kreativesquadz.billkit.model.settings.POSSettings
import com.kreativesquadz.billkit.repository.InventoryRepository
import com.kreativesquadz.billkit.repository.UserSettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
