package com.kreativesquadz.billkit.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kreativesquadz.billkit.Dao.UserSettingDao
import com.kreativesquadz.billkit.Database.AppDatabase
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.api.ApiResponse
import com.kreativesquadz.billkit.api.common.NetworkBoundResource
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.UserSetting
import com.kreativesquadz.billkit.model.settings.PdfSettings
import com.kreativesquadz.billkit.model.settings.ThermalPrinterSetup
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class UserSettingRepository @Inject constructor(val db: AppDatabase) {
    private val userSettingDao: UserSettingDao = db.userSettingDao()


    fun loadUserSetting(userId : Long): LiveData<Resource<UserSetting>> {
        return object : NetworkBoundResource<UserSetting, UserSetting>() {
            override fun saveCallResult(item: UserSetting) {
                try {
                    db.runInTransaction {
                        userSettingDao.insert(item)
                        Timber.tag("Response").e(item.toString())
                        Log.e("Response", item.toString())
                    }
                } catch (ex: Exception) {
                    //Util.showErrorLog("Error at ", ex)
                    Timber.tag("Error at loadCompanyDetails()").e(ex.toString())
                    Log.e("Error", ex.toString())

                }
            }

            override fun shouldFetch(data: UserSetting?): Boolean {
                return true
            }

            override fun loadFromDb(): LiveData<UserSetting> {
                return userSettingDao.getUserById(userId)
            }

            override fun createCall(): LiveData<ApiResponse<UserSetting>> {
                return ApiClient.getApiService().loadUserSetting(userId)
            }
        }.asLiveData()
    }

//    suspend fun updateDiscount(userSetting: UserSetting?): LiveData<Boolean> {
//        val statusLiveData = MutableLiveData<Boolean>()
//        try {
//            val response = ApiClient.getApiService().updateUserSetting(userSetting)
//            if (response.isSuccessful) {
//                Timber.tag("Response").d(response.body().toString())
//                statusLiveData.value = true
//            } else {
//                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
//                statusLiveData.value = false
//
//
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//            Resource.error("Network error: ${e.message}", null)
//            statusLiveData.value = false
//        }
//        Timber.tag("Response").e(statusLiveData.value.toString())
//        return statusLiveData
//    }

    fun insert(userSetting: UserSetting) {
        userSettingDao.insert(userSetting)
    }
    fun getUserSetting(userId: Long): LiveData<UserSetting> {
        return userSettingDao.getUserById(userId)
    }

    fun insertPdfSetting(pdfSetting: PdfSettings) : Long {
       return userSettingDao.insertPdfSettings(pdfSetting)
    }

    fun getPdfSetting(userId: Long): PdfSettings? {
        return userSettingDao.getUserPdfSettingsById(userId)
    }


    suspend fun updatePdfSetting(userId: Long, pdfCompanyInfo: String, pdfItemTable: String, pdfFooter: String) {
            userSettingDao.updatePdfSettings(userId, pdfCompanyInfo, pdfItemTable, pdfFooter)
      //  userSettingDao.updatePdfSettings(pdfSetting)
    }



    fun insertPrinterSetting(thermalPrinterSetup: ThermalPrinterSetup) : Long {
       return userSettingDao.insertPrinterSettings(thermalPrinterSetup)
    }



    fun getPrinterSetting(userId: Long): ThermalPrinterSetup? {
        return userSettingDao.getUserPrinterSettingsById(userId)
    }


    suspend fun updatePrinterSetting(userId: Long, printerSize : String, printerMode : String, fontSize : String, enableAutoPrint : Boolean, openCashDrawer : Boolean, disconnectAfterPrint : Boolean, autoCutAfterPrint : Boolean, defaultPrinterAddress : String,defaultprinterName : String ) {
            userSettingDao.updatePrinterSettings(userId, printerSize, printerMode, fontSize, enableAutoPrint, openCashDrawer, disconnectAfterPrint, autoCutAfterPrint, defaultPrinterAddress, defaultprinterName)
      //  userSettingDao.updatePdfSettings(pdfSetting)
    }



}