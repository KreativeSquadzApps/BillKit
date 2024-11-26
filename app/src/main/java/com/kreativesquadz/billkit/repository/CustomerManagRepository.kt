package com.kreativesquadz.billkit.repository


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kreativesquadz.billkit.Dao.CustomerDao
import com.kreativesquadz.billkit.Dao.InvoiceDao
import com.kreativesquadz.billkit.Database.AppDatabase
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.api.ApiResponse
import com.kreativesquadz.billkit.api.common.NetworkBoundResource
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.CustomerCreditDetail
import com.kreativesquadz.billkit.model.Invoice
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class CustomerManagRepository @Inject constructor( private val db : AppDatabase) {
    private val customerDao: CustomerDao = db.customerDao()
    private val creditDetailsDao = db.creditDetailsDao()

    fun loadAllCustomers(): LiveData<Resource<List<Customer>>> {
        return object : NetworkBoundResource<List<Customer>, List<Customer>>() {
            override fun saveCallResult(item: List<Customer>) {
                try {
                    db.runInTransaction {
                        customerDao.deleteCustomer()
                        customerDao.insertCustomerList(item)
                        Timber.d("loadAllCustomers() All Customers loaded ")
                    }
                } catch (ex: Exception) {
                    Timber.tag("Error at loadAllCustomers()").e(ex.toString())
                }
            }

            override fun shouldFetch(data: List<Customer>?): Boolean {
                return true
            }

            override fun loadFromDb(): LiveData<List<Customer>> {
                return customerDao.getCustomers()
            }

            override fun createCall(): LiveData<ApiResponse<List<Customer>>> {
                return ApiClient.getApiService().loadAllCustomers()
            }
        }.asLiveData()
    }

    suspend fun addCustomer(customer: Customer): LiveData<Boolean> {
        val statusLiveData = MutableLiveData<Boolean>()
        customerDao.insertCustomer(customer)
        statusLiveData.value = true
        return statusLiveData
    }

    fun getCustomer(id: String): Customer {
        return customerDao.getCustomer(id)
    }

    fun getCustomerLiveData(id: String): LiveData<Customer> {
        return customerDao.getCustomerLiveData(id)
    }


    suspend fun getUnsyncedCustomers(): List<Customer> {
        return customerDao.getUnsyncedCustomers()
    }

    suspend fun markCustomerAsSynced(customer: Customer) {
        customerDao.update(customer.copy(isSynced = 1))
    }

    suspend fun updateCreditAmount(id: Long, creditAmount: Double,type : String) {
        if (type.isEmpty()){
            customerDao.updateCreditAmount(id, creditAmount)
        }else{
            customerDao.decrementCreditAmount(id,creditAmount)
        }
        creditDetailsDao.insertCustomerCreditDetail(
            CustomerCreditDetail(
                customerId = id,
                creditDate = System.currentTimeMillis().toString(),
                creditType = "Manual",
                creditAmount = creditAmount
            )
        )
    }

    suspend fun removeCreditAmount(id: Long, creditAmount: Double, type: String) {
        customerDao.removeCreditAmount(id, creditAmount)
        creditDetailsDao.insertCustomerCreditDetail(
            CustomerCreditDetail(
                customerId = id,
                creditDate = System.currentTimeMillis().toString(),
                creditType = type,
                creditAmount = creditAmount
            )
        )
    }

    suspend fun updateCustomer(customer: Customer) : LiveData<Boolean>  {
        val statusLiveData = MutableLiveData<Boolean>()
        customerDao.update(customer)
        statusLiveData.value = true
        return statusLiveData
    }

    fun deleteCustomer(id: Long)  {
        customerDao.deleteCustomer(id)
    }

}