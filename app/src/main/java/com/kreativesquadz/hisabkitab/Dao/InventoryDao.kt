package com.kreativesquadz.hisabkitab.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kreativesquadz.hisabkitab.model.Category
import com.kreativesquadz.hisabkitab.model.Product

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

    @Query("DELETE FROM categories WHERE categoryId = :id")
    fun deleteCategory(id : Long)

    //For Products

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Query("SELECT * FROM products WHERE userId = :userId")
    fun getProductsForUser(userId: Long): LiveData<List<Product>>

    @Query("SELECT * FROM products WHERE productTax = :productTax")
    fun getProductsForUserByTax(productTax: Double): LiveData<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProductList(product: List<Product>)

    @Query("SELECT * FROM products WHERE isSynced = 0")
    suspend fun getUnsyncedProducts(): List<Product>

    @Update
    suspend fun updateProduct(product: Product)

    @Query("DELETE FROM products")
    fun deleteProductList()

    @Query("DELETE FROM products WHERE productId = :id")
    fun deleteProduct(id : Long)

    @Query("UPDATE products SET productStock = productStock - :quantity WHERE productName = :productName")
    fun decrementProductStock(productName: String?, quantity: Int)

    @Query("UPDATE products SET productStock = productStock + :quantity WHERE productName = :productName")
    fun incrementProductStock(productName: String?, quantity: Int)





    @Query("UPDATE products SET productStock = productStock + :quantity WHERE productId = :id")
    fun updateProductStock(id: Long?, quantity: Int)


    @Query("SELECT * FROM products WHERE productBarcode = :barcode")
    fun selectProductByBarcode(barcode: String): Product


    @Query("SELECT * FROM products WHERE productId = :productId")
    fun selectProductById( productId: Long): Product

    @Query("SELECT * FROM products WHERE productName = :productName")
    fun getProductByName(productName: String): Product?



}