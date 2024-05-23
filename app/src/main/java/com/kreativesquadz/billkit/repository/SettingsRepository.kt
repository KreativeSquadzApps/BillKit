package com.kreativesquadz.billkit.repository

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

    fun loadCompanyDetails(): LiveData<Resource<List<CompanyDetails>>> {
        return object : NetworkBoundResource<List<CompanyDetails>, List<CompanyDetails>>() {
            override fun saveCallResult(item: List<CompanyDetails>) {
                try {
                    db.runInTransaction {
                        companyDao.insertCompanyDetails(item)
                        Timber.tag("Response").e(item.toString())
                    }
                } catch (ex: Exception) {
                    //Util.showErrorLog("Error at ", ex)
                    Timber.tag("Error at loadCompanyDetails()").e(ex.toString())
                }
            }

            override fun shouldFetch(data: List<CompanyDetails>?): Boolean {
                return true
            }

            override fun loadFromDb(): LiveData<List<CompanyDetails>> {
                return companyDao.getCompanyDetails()
            }

            override fun createCall(): LiveData<ApiResponse<List<CompanyDetails>>> {
                return ApiClient.getApiService().loadCompanyDetails()
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