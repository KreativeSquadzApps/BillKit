package com.kreativesquadz.billkit.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kreativesquadz.billkit.model.CompanyDetails

@Dao
interface CompanyDetailsDao {
    @Query("SELECT * FROM companyDetails WHERE userId = :userId")
    fun getCompanyDetails(userId: Long): LiveData<CompanyDetails>

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun insertCompanyDetails(companyDetails: CompanyDetails)

    @Update
    fun update(companyDetails: CompanyDetails)


}