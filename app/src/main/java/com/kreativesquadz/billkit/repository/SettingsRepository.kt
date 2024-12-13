package com.kreativesquadz.billkit.repository

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kreativesquadz.billkit.Database.AppDatabase
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.api.ApiResponse
import com.kreativesquadz.billkit.api.ApiStatus
import com.kreativesquadz.billkit.api.common.NetworkBoundResource
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.InvoicePrefixNumber
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class SettingsRepository @Inject constructor(val db : AppDatabase) {
    val companyDao = db.companyDetailsDao()

    fun insertCompanyDetails(companyDetails: CompanyDetails){
        companyDao.insertCompanyDetails(companyDetails)
    }
    fun loadCompanyDetails(userId : Long): LiveData<Resource<CompanyDetails>> {
        return object : NetworkBoundResource<CompanyDetails, CompanyDetails>() {
            override fun saveCallResult(item: CompanyDetails) {
                try {
                    db.runInTransaction {
                        companyDao.insertCompanyDetails(item)
                        Timber.tag("Response").e(item.toString())
                        Log.e("Response", item.toString())
                    }
                } catch (ex: Exception) {
                    //Util.showErrorLog("Error at ", ex)
                    Timber.tag("Error at loadCompanyDetails()").e(ex.toString())
                    Log.e("Error", ex.toString())

                }
            }

            override fun shouldFetch(data: CompanyDetails?): Boolean {
                return true
            }

            override fun loadFromDb(): LiveData<CompanyDetails> {
                return companyDao.getCompanyDetails(userId)
            }

            override fun createCall(): LiveData<ApiResponse<CompanyDetails>> {
                return ApiClient.getApiService().loadCompanyDetails(userId)
            }
        }.asLiveData()
    }
    fun loadCompanyDetailsDb(userId : Long) : LiveData<CompanyDetails> = companyDao.getCompanyDetails(userId)
    fun getCompanyDetails(userId: Long): CompanyDetails? {
        return companyDao.getCompanyDetailsById(userId)
    }
    suspend fun updateCompanyDetails(companyDetails: CompanyDetails?): LiveData<Boolean> {
        val statusLiveData = MutableLiveData<Boolean>()
        try {
            val response = ApiClient.getApiService().updateCompanyDetails(companyDetails)
            if (response.isSuccessful) {
                Timber.tag("Response").d(response.body().toString())
                statusLiveData.value = true
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                statusLiveData.value = false

            }
        } catch (e: IOException) {
            e.printStackTrace()
            Resource.error("Network error: ${e.message}", null)
            statusLiveData.value = false
        }
        Timber.tag("Response").e(statusLiveData.value.toString())
        return statusLiveData
    }
    suspend fun update(companyDetails: CompanyDetails){
        companyDao.update(companyDetails)
    }
    suspend fun uploadCompanyImage(userId: RequestBody, image: MultipartBody.Part?): Result<ApiStatus> {
        return try {
            val response = ApiClient.getApiService().updateCompanyImage(userId, image)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to upload image: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    fun insertInvoicePrefixNumber(invoicePrefixNumber: InvoicePrefixNumber): Long{
        val result = companyDao.insertInvoicePrefixNumber(invoicePrefixNumber)
        companyDao.updateInvoiceNumberAndPrefix(invoicePrefixNumber.userId,invoicePrefixNumber.invoicePrefix,invoicePrefixNumber.invoiceNumber.toInt())
        return result
    }
    suspend fun updateInvoiceNumber(userId: Long, invoiceNumber: Int){
        companyDao.updateInvoiceNumber(userId, invoiceNumber)
    }
    fun updateInvoiceNumberAndPrefix(userId: Long,id: Long, invoicePrefix: String , invoiceNumber: Int){
        companyDao.updateInvoiceNumberAndPrefix(userId, invoicePrefix,invoiceNumber)
        companyDao.updateInvoiceNumberAndPrefixTable(id,invoicePrefix,invoiceNumber)
    }
    suspend fun deleteInvoicePrefixNumber(id: Long){
       val response = ApiClient.getApiService().deleteInvoicePrefixNumber(id)
       if (response.isSuccessful){
           companyDao.deleteInvoicePrefixNumber(id)
       }
    }
    fun loadInvoicePrefixNumberList(userId : Long): LiveData<Resource<List<InvoicePrefixNumber>>> {
        return object : NetworkBoundResource<List<InvoicePrefixNumber>, List<InvoicePrefixNumber>>() {
            override fun saveCallResult(item: List<InvoicePrefixNumber>) {
                try {
                    db.runInTransaction {
                        companyDao.deleteInvoicePrefixNumberList(userId)
                        companyDao.insertInvoicePrefixNumberList(item)
                        Log.e("Responseuuuu", item.toString())
                    }
                } catch (ex: Exception) {
                    //Util.showErrorLog("Error at ", ex)
                    Log.e("Error", ex.toString())

                }
            }

            override fun shouldFetch(data: List<InvoicePrefixNumber>?): Boolean {
                return true
            }

            override fun loadFromDb(): LiveData<List<InvoicePrefixNumber>> {
                return companyDao.getInvoicePrefixNumberList(userId)
            }

            override fun createCall(): LiveData<ApiResponse<List<InvoicePrefixNumber>>> {
                return ApiClient.getApiService().loadInvoicePrefixNumber(userId)
            }
        }.asLiveData()
    }
    fun getInvoicePrefixNumber(id: Long): InvoicePrefixNumber {
       return companyDao.getInvoicePrefixNumber(id)
    }
    fun getInvoicePrefixNumberWithPrefix(invoicePrefix: String): InvoicePrefixNumber {
       return companyDao.getInvoicePrefixNumberWithPrefix(invoicePrefix)
    }


    suspend fun hardRest(userId: Long,context: Context) {
        try {
            val response = ApiClient.getApiService().hardReset(userId)
            if (response.isSuccessful) {
                val database = AppDatabase.getInstance(context)
                database?.clearAllTables()
            } else {
                // Handle specific error response here, maybe show a message to the user
            }
        } catch (e: Exception) {
            Log.e("Exception", "An error occurred: ${e.localizedMessage}")
            // Handle network or other unexpected errors
        }
    }









}