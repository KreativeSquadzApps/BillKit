package com.kreativesquadz.billkit.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kreativesquadz.billkit.model.Product
import com.kreativesquadz.billkit.model.Staff

@Dao
interface StaffDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: Staff)

    @Query("SELECT * FROM staff WHERE adminId = :adminId")
    fun getStaffByUser(adminId: Long): LiveData<List<Staff>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStaffList(staffList: List<Staff>)

    @Query("SELECT * FROM staff WHERE isSynced = 0")
    suspend fun getUnsyncedStaff(): List<Staff>

    @Update
    suspend fun updateStaff(staff: Staff)

    @Query("DELETE FROM staff")
    fun deleteStaffList()

    @Query("DELETE FROM staff WHERE id = :id")
    fun deleteStaffById(id: Long)


    @Query("SELECT * FROM staff WHERE id = :id")
    fun selectStaffById(id: Long): Staff
}