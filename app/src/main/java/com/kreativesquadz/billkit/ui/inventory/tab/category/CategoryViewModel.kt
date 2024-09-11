package com.kreativesquadz.billkit.ui.inventory.tab.category

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.Product
import com.kreativesquadz.billkit.repository.InventoryRepository
import com.kreativesquadz.billkit.worker.DeleteCategoryWorker
import com.kreativesquadz.billkit.worker.DeleteProductWorker
import com.kreativesquadz.billkit.worker.SyncCategoriesWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(private val repository: InventoryRepository)  : ViewModel() {
    private var _categoriesStatus = MutableLiveData<ApiStatus>()
    val categoriesStatus: LiveData<ApiStatus> get() = _categoriesStatus
    lateinit var category : LiveData<Resource<List<Category>>>
    lateinit var products : LiveData<Resource<List<Product>>>
    private val _isExpanded = MutableStateFlow(false)
    val isExpanded: StateFlow<Boolean> get() = _isExpanded
    fun getProducts(){
        products = repository.loadAllProduct(Config.userId)
    }

    fun addcategoryObj(context: Context,categoryName : String) {
        Log.e("categoryName",categoryName)

        val categoryObj = Category(userId = Config.userId, categoryName = categoryName)
        viewModelScope.launch {
            category.value?.data?.let {
                it.forEach {
                    if(it.categoryName == categoryName){
                        val apiStatus = ApiStatus(400,"Category Already Exists" )
                        _categoriesStatus.value = apiStatus
                        return@launch
                    }
                }
            }
            repository.addCategory(categoryObj)
            scheduleCategorySync(context)
            val apiStatus = ApiStatus(200,"Category added Successfully" )
            _categoriesStatus.value = apiStatus
        }
    }

    fun getCategories(){
        category = repository.loadAllCategory(Config.userId)
    }

    fun scheduleCategorySync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncCategoriesWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "categorySyncWork",
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )
    }

       fun setExpandedState(isExpanded: Boolean) {
        viewModelScope.launch {
            _isExpanded.value = isExpanded
        }
    }

    fun deleteCategory( context: Context,id : Long){
        viewModelScope.launch {
            repository.deleteCategory(id)
            deleteCategoryWork(context,id.toString())
        }
    }

    private fun deleteCategoryWork (context: Context, id: String ) {
        val data = Data.Builder()
            .putString("id",id)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<DeleteCategoryWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "deleteCategoryWorker",
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )
    }


}