package com.kreativesquadz.hisabkitab.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kreativesquadz.hisabkitab.model.CustomerCreditDetail
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditDetailsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerCreditDetail(customerCreditDetail: CustomerCreditDetail)


    @Query("SELECT * FROM customerCreditDetails WHERE customerId = :customerId ORDER BY id DESC")
    fun getAllCustomerCreditDetails(customerId: Long): Flow<List<CustomerCreditDetail>>

    @Query("DELETE FROM customerCreditDetails WHERE id = :id")
    suspend fun deleteCustomerCreditDetail(id: Long)

    @Query("SELECT * FROM customerCreditDetails WHERE id = :id")
    fun getCustomerCreditDetail(id: Long): CustomerCreditDetail


}
