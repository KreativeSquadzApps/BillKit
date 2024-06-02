package com.kreativesquadz.billkit.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kreativesquadz.billkit.Dao.InventoryDao
import com.kreativesquadz.billkit.Database.AppDatabase
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.api.ApiResponse
import com.kreativesquadz.billkit.api.common.NetworkBoundResource
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Category
import javax.inject.Inject

class InventoryRepository @Inject constructor(private val db : AppDatabase) {
    private val inventoryDao: InventoryDao = db.inventoryDao()

    fun loadAllCategory(userId:Long): LiveData<Resource<List<Category>>> {
        return object : NetworkBoundResource<List<Category>, List<Category>>() {
            override fun saveCallResult(item: List<Category>) {
                try {
                    db.runInTransaction {
                        inventoryDao.deleteCategoryList()
                        inventoryDao.insertCategoryList(item)
                    }
                } catch (ex: Exception) {
                    Log.e("TAG", ex.toString())
                }
            }

            override fun shouldFetch(data: List<Category>?): Boolean {
                return true
            }

            override fun loadFromDb(): LiveData<List<Category>> {
                return inventoryDao.getCategoriesForUser(userId)
            }

            override fun createCall(): LiveData<ApiResponse<List<Category>>> {
                return ApiClient.getApiService().loadCategories(userId)
            }
        }.asLiveData()
    }

    suspend fun addCategory(category: Category) : LiveData<Boolean>  {
        val statusLiveData = MutableLiveData<Boolean>()
        inventoryDao.insert(category)
        statusLiveData.value = true
        return statusLiveData
    }
    suspend fun getUnsyncedCategories(): List<Category> {
        return inventoryDao.getUnsyncedCategories()
    }

    suspend fun markCategoriesAsSynced(category: Category) {
        inventoryDao.update(category)
    }



}