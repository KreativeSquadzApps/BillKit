package com.kreativesquadz.hisabkitab.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kreativesquadz.hisabkitab.model.settings.Packaging


@Dao
interface BillSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPackaging(packaging: Packaging)

      @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPackagingList(packagingList: List<Packaging>)

    @Query("SELECT * FROM packaging WHERE id = :id")
    fun getPackagingById(id: Int): Packaging

    @Query("SELECT * FROM packaging WHERE userId = :userId")
    fun getPackagingByUserId(userId: Int): LiveData<List<Packaging>>

    @Query("SELECT * FROM packaging WHERE isSynced = 0")
    suspend fun getUnsyncedPackaging(): List<Packaging>

    @Update
    suspend fun update(packaging: Packaging)

    @Query("DELETE FROM packaging")
    fun deleteAllPackaging()

    @Query("DELETE FROM packaging WHERE id = :id")
    fun deletePackagingById(id: Int)



}