package com.kreativesquadz.billkit.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kreativesquadz.billkit.Dao.CreditNoteDao
import com.kreativesquadz.billkit.Dao.InventoryDao
import com.kreativesquadz.billkit.Database.AppDatabase
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.api.ApiResponse
import com.kreativesquadz.billkit.api.common.NetworkBoundResource
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Category
import com.kreativesquadz.billkit.model.CreditNote
import com.kreativesquadz.billkit.model.Product
import com.kreativesquadz.billkit.model.request.ProductStockRequest
import javax.inject.Inject

class InventoryRepository @Inject constructor(private val db : AppDatabase) {
    private val inventoryDao: InventoryDao = db.inventoryDao()

    //For Category
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

     fun deleteCategory(id: Long)  {
        inventoryDao.deleteCategory(id)
    }




    //For Products

    fun loadAllProduct(userId:Long): LiveData<Resource<List<Product>>> {
        return object : NetworkBoundResource<List<Product>, List<Product>>() {
            override fun saveCallResult(item: List<Product>) {
                try {
                    db.runInTransaction {
                        inventoryDao.deleteProductList()
                        inventoryDao.insertProductList(item)
                    }
                } catch (ex: Exception) {
                    Log.e("TAG", ex.toString())
                }
            }

            override fun shouldFetch(data: List<Product>?): Boolean {
                return true
            }

            override fun loadFromDb(): LiveData<List<Product>> {
                return inventoryDao.getProductsForUser(userId)
            }

            override fun createCall(): LiveData<ApiResponse<List<Product>>> {
                return ApiClient.getApiService().loadProducts(userId)
            }
        }.asLiveData()
    }

    suspend fun addProduct(product: Product) : LiveData<Boolean>  {
        val statusLiveData = MutableLiveData<Boolean>()
        inventoryDao.insertProduct(product)
        statusLiveData.value = true
        return statusLiveData
    }

    suspend fun getUnsyncedProducts(): List<Product> {
        return inventoryDao.getUnsyncedProducts()
    }

    suspend fun markproductsAsSynced(product: Product) {
        inventoryDao.updateProduct(product)
    }


    fun getProductByBarcode(barcode: String): Product {
        return inventoryDao.selectProductByBarcode(barcode)
    }

     fun deleteProduct(id: Long)  {
        inventoryDao.deleteProduct(id)
     }

     fun updateProductStock(id: Long, quantity: Int) {
        inventoryDao.updateProductStock(id,quantity)
     }

    suspend fun updateProduct(product: Product) {
        inventoryDao.updateProduct(product)
    }

    fun getProduct(id: Long): Product {
        return inventoryDao.selectProductById(id)
    }





    //For Credit Notes


     fun decrementProductStock(productName: String?, quantity: Int) {

        inventoryDao.decrementProductStock(productName,quantity)
    }



}