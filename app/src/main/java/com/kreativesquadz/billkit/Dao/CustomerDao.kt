package com.kreativesquadz.billkit.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kreativesquadz.billkit.model.Customer

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers")
    fun getCustomers(): LiveData<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCustomerList(customer: List<Customer>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCustomer(customer: Customer)

    @Query("DELETE FROM customers")
    fun deleteCustomer()

}