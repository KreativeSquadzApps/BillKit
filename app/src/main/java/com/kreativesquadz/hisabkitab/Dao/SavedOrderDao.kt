package com.kreativesquadz.hisabkitab.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kreativesquadz.hisabkitab.model.SavedOrderEntity

@Dao
interface SavedOrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertSavedOrder(order: SavedOrderEntity): Long

     @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertAllSavedOrders(orders: List<SavedOrderEntity>)

    @Query("SELECT * FROM saved_order")
    fun getSavedOrders(): LiveData<List<SavedOrderEntity>>




    @Query("DELETE FROM saved_order")
    fun deleteAllSavedOrders()

    @Query("DELETE FROM saved_order WHERE orderId = :orderId")
    fun deleteSavedOrderById(orderId: Long)




}