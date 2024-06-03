package com.kreativesquadz.billkit.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kreativesquadz.billkit.model.Category
import com.kreativesquadz.billkit.model.Product

@Dao
interface InventoryDao {

    //For Category
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category)

    @Query("SELECT * FROM categories WHERE userId = :userId")
     fun getCategoriesForUser(userId: Long): LiveData<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategoryList(category: List<Category>)

    @Query("SELECT * FROM categories WHERE isSynced = 0")
    suspend fun getUnsyncedCategories(): List<Category>

    @Update
    suspend fun update(category: Category)

    @Query("DELETE FROM categories")
    fun deleteCategoryList()

    //For Products

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Query("SELECT * FROM products WHERE userId = :userId")
    fun getProductsForUser(userId: Long): LiveData<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProductList(product: List<Product>)

    @Query("SELECT * FROM products WHERE isSynced = 0")
    suspend fun getUnsyncedProducts(): List<Product>

    @Update
    suspend fun updateProduct(product: Product)

    @Query("DELETE FROM products")
    fun deleteProductList()


}