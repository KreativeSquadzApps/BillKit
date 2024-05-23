package com.kreativesquadz.billkit.repository


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kreativesquadz.billkit.Dao.CustomerDao
import com.kreativesquadz.billkit.Database.AppDatabase
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.api.ApiResponse
import com.kreativesquadz.billkit.api.common.NetworkBoundResource
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Customer
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class CustomerManagRepository @Inject constructor(val context: Context, private val db : AppDatabase) {
    private val customerDao: CustomerDao = db.customerDao()


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
        try {
            val response = ApiClient.getApiService().addCustomer(customer)
            if (response.isSuccessful) {
                statusLiveData.value = true
                customerDao.insertCustomer(customer)
                Timber.tag("Response").d(response.body().toString())

            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                statusLiveData.value = false
                Timber.tag("Error").d(errorMessage)
            }

            Toast.makeText(context, response.body()?.message, Toast.LENGTH_SHORT).show()

        } catch (e: IOException) {
            e.printStackTrace()
            Resource.error("Network error: ${e.message}", null)
            statusLiveData.value = false
        }

        return statusLiveData
    }

    fun getCustomer(id: String): Customer {
        return customerDao.getCustomer(id)
    }


}