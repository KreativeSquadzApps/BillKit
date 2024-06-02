package com.kreativesquadz.billkit.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kreativesquadz.billkit.model.Category

@Dao
interface InventoryDao {
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
    fun deleteCategoryList()}