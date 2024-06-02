package com.kreativesquadz.billkit.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kreativesquadz.billkit.Database.AppDatabase
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.api.ApiResponse
import com.kreativesquadz.billkit.api.common.NetworkBoundResource
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.CompanyDetails
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class SettingsRepository @Inject constructor(val db : AppDatabase) {
    val companyDao = db.companyDetailsDao()

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


}