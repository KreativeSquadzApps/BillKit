package com.kreativesquadz.billkit.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.GST


@Dao
interface GSTDao {
    @Insert
    fun insertGst(gst: GST)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGstList(gst: List<GST>)

    @Query("SELECT * FROM gst_table")
    fun getAllGST(): LiveData<List<GST>>

    @Query("SELECT * FROM gst_table WHERE id = :id")
    fun getGSTById(id: Int): GST

    @Query("SELECT * FROM gst_table WHERE userId = :userId")
    fun getGSTByUserId(userId: Int): LiveData<List<GST>>

    @Query("SELECT * FROM gst_table WHERE isSynced = 0")
    suspend fun getUnsyncedGST(): List<GST>

    @Update
    suspend fun update(gst: GST)

    @Query("UPDATE gst_table SET taxAmount = :amount WHERE id = :id")
    fun updateGSTAmount(id: Int, amount: Double)


    @Query("DELETE FROM gst_table")
    fun deleteAllGST()



}