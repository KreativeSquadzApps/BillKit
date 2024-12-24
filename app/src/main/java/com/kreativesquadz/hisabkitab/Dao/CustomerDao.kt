package com.kreativesquadz.hisabkitab.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kreativesquadz.hisabkitab.model.Customer

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers")
    fun getCustomers(): LiveData<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCustomerList(customer: List<Customer>)

    @Query("SELECT COUNT(*) FROM customers WHERE customerName = :name OR shopContactNumber = :number")
    suspend fun isCustomerExists(name: String, number: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomer(id: String): Customer

    @Query("SELECT * FROM customers WHERE customerName = :customerName")
   suspend fun getCustomerByName(customerName: String): Customer

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerLiveData(id: String): LiveData<Customer>

    @Query("DELETE FROM customers")
    fun deleteCustomer()

    @Query("SELECT * FROM customers WHERE isSynced = 0")
    suspend fun getUnsyncedCustomers(): List<Customer>

    @Update
    suspend fun update(customer: Customer)

    @Query("UPDATE customers SET creditAmount = creditAmount + :creditedAmount WHERE id = :id")
    fun updateCreditAmount(id: Long?, creditedAmount: Double)

    @Query("UPDATE customers SET creditAmount = creditAmount - :creditedAmount WHERE id = :id")
    suspend fun decrementCreditAmount(id: Long, creditedAmount: Double): Int

    @Query("UPDATE customers SET creditAmount = creditAmount - :creditedAmount WHERE id = :id")
    fun removeCreditAmount(id: Long?, creditedAmount: Double)


    @Query("DELETE FROM customers WHERE id = :id")
    fun deleteCustomer(id : Long)




}