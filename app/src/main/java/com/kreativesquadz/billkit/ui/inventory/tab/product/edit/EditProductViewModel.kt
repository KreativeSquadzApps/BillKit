package com.kreativesquadz.billkit.ui.inventory.tab.product.edit

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.ApiStatus
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Category
import com.kreativesquadz.billkit.model.settings.GST
import com.kreativesquadz.billkit.model.Product
import com.kreativesquadz.billkit.repository.GstTaxRepository
import com.kreativesquadz.billkit.repository.InventoryRepository
import com.kreativesquadz.billkit.worker.DeleteProductWorker
import com.kreativesquadz.billkit.worker.UpdateCompleteProductWorker
import com.kreativesquadz.billkit.worker.UpdateProductStockWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProductViewModel @Inject constructor(val inventoryRepository: InventoryRepository,
                                               val gstTaxRepository: GstTaxRepository
) : ViewModel() {
    private var _productsStatus = MutableLiveData<ApiStatus>()
    val productsStatus: LiveData<ApiStatus> get() = _productsStatus

    lateinit var category: LiveData<Resource<List<Category>>>
    private val _barcodeText = MutableLiveData<String>()
    val barcodeText: LiveData<String> get() = _barcodeText
    lateinit var gstTax: LiveData<Resource<List<GST>>>


    fun getGstTax() {
        gstTax = gstTaxRepository.loadAllgstTax(Config.userId.toInt())
    }

    fun setBarcodeText(text: String) {
        _barcodeText.value = text
    }

    fun updateproductObj(context: Context, product: Product) {
        viewModelScope.launch {
         inventoryRepository.updateProduct(product)
         updateProductWorker(context, product.productId.toString())
            val apiStatus = ApiStatus(200,"Product Updated Successfully" )
            _productsStatus.value = apiStatus
        }


    }


    fun getCategories(): LiveData<List<String>> {
        category = inventoryRepository.loadAllCategory(Config.userId)
        return category.map {
            it.data?.map { it.categoryName } ?: emptyList()
        }
    }

    fun deleteProduct( context: Context,id : Long){
        viewModelScope.launch {
            inventoryRepository.deleteProduct(id)
            deleteProductWork(context, id.toString())
        }
    }
     fun updateProductStock( context: Context,id : Long ,stock : Int){
        viewModelScope.launch {
            inventoryRepository.updateProductStock(id,stock)
            updateProductStockWork(context, id.toString(),stock)
        }
    }


    private fun deleteProductWork (context: Context, id: String ) {
        val data = Data.Builder()
            .putString("id",id)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<DeleteProductWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "deleteProductWorker",
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )
    }


    private fun updateProductStockWork (context: Context, id: String , stock : Int ) {
                val data = Data.Builder()
                    .putString("id",id)
                    .putInt("stock",stock)
                    .build()

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val syncWorkRequest = OneTimeWorkRequestBuilder<UpdateProductStockWorker>()
                    .setConstraints(constraints)
                    .setInputData(data)
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    "updateProductStockWorker",
                    ExistingWorkPolicy.KEEP,
                    syncWorkRequest
                )
            }

    private fun updateProductWorker (context: Context, productId: String) {
        val data = Data.Builder()
            .putString("id",productId)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val updateproductWorkRequest = OneTimeWorkRequestBuilder<UpdateCompleteProductWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "updateProductWorker",
            ExistingWorkPolicy.REPLACE,
            updateproductWorkRequest
        )
    }


}