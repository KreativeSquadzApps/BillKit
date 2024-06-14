package com.kreativesquadz.billkit.ui.inventory.tab.product.add

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.ApiStatus
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Category
import com.kreativesquadz.billkit.model.Product
import com.kreativesquadz.billkit.repository.InventoryRepository
import com.kreativesquadz.billkit.worker.SyncProductsWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddProductViewModel @Inject constructor(val inventoryRepository: InventoryRepository) : ViewModel() {
    private var _productsStatus = MutableLiveData<ApiStatus>()
    val productsStatus: LiveData<ApiStatus> get() = _productsStatus

    lateinit var products : LiveData<Resource<List<Product>>>
    lateinit var category : LiveData<Resource<List<Category>>>
    private val _barcodeText = MutableLiveData<String>()
    val barcodeText: LiveData<String> get() = _barcodeText

    fun setBarcodeText(text: String) {
        _barcodeText.value = text
    }

    fun addproductObj(context: Context, productName : String, product: Product) {
        if(productName.isEmpty()){
            val apiStatus = ApiStatus(400,"Product Name cannot be empty" )
            _productsStatus.value = apiStatus
            return
        }
        viewModelScope.launch {
            products.value?.data?.let {
                it.forEach {
                    if(it.productName == productName){
                        val apiStatus = ApiStatus(400,"Product Already Exists" )
                        _productsStatus.value = apiStatus
                        return@launch
                    }
                }
            }
            inventoryRepository.addProduct(product)
            scheduleProductSync(context)
            val apiStatus = ApiStatus(200,"Product added Successfully" )
            _productsStatus.value = apiStatus
        }
    }

    fun getProducts(){
        products = inventoryRepository.loadAllProduct(Config.userId)
    }

    fun getCategories(): LiveData<List<String>> {
        category = inventoryRepository.loadAllCategory(Config.userId)
        return category.map {
            it.data?.map { it.categoryName } ?: emptyList()
        }
    }

    fun scheduleProductSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncProductsWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "productsSyncWork",
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )
    }




}