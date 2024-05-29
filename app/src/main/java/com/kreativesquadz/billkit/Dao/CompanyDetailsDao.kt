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

    @Query("SELECT * FROM companyDetails")
    fun getCompanyDetails(): LiveData<List<CompanyDetails>>

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun insertCompanyDetails(companyDetails: List<CompanyDetails>)


    @Update
    fun update(companyDetails: CompanyDetails)


}