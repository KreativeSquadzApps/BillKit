package com.kreativesquadz.hisabkitab.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.kreativesquadz.hisabkitab.Dao.UserSettingDao
import com.kreativesquadz.hisabkitab.Database.AppDatabase
import com.kreativesquadz.hisabkitab.api.ApiClient
import com.kreativesquadz.hisabkitab.api.ApiResponse
import com.kreativesquadz.hisabkitab.api.common.NetworkBoundResource
import com.kreativesquadz.hisabkitab.api.common.common.Resource
import com.kreativesquadz.hisabkitab.model.settings.UserSetting
import com.kreativesquadz.hisabkitab.model.settings.InvoicePrinterSettings
import com.kreativesquadz.hisabkitab.model.settings.POSSettings
import com.kreativesquadz.hisabkitab.model.settings.PdfSettings
import com.kreativesquadz.hisabkitab.model.settings.ThermalPrinterSetup
import timber.log.Timber
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
    fun insert(userSetting: UserSetting) {
        userSettingDao.insert(userSetting)
    }
    fun getUserSetting(userId: Long): LiveData<UserSetting> {
        return userSettingDao.getUserById(userId)
    }

 fun getUserSettingById(userId: Long): UserSetting {
        return userSettingDao.getUserSettingById(userId)
    }



    fun insertPdfSetting(pdfSetting: PdfSettings) : Long {
       return userSettingDao.insertPdfSettings(pdfSetting)
    }
    fun getPdfSetting(userId: Long): PdfSettings? {
        return userSettingDao.getUserPdfSettingsById(userId)
    }
    suspend fun updatePdfSetting(userId: Long, pdfCompanyInfo: String, pdfItemTable: String, pdfColor: String?, pdfFooter: String) {
            userSettingDao.updatePdfSettings(userId, pdfCompanyInfo, pdfItemTable,pdfColor ,pdfFooter)
      //  userSettingDao.updatePdfSettings(pdfSetting)
    }



    fun insertInvoicePrinterSetting(invoicePrinterSetting: InvoicePrinterSettings) : Long {
       return userSettingDao.insertInvoicePrinterSettings(invoicePrinterSetting)
    }
    fun getInvoicePrinterSetting(userId: Long): InvoicePrinterSettings? {
        return userSettingDao.getInvoicePrinterSettingsById(userId)
    }
    suspend fun updateInvoicePrinterSetting(userId: Long, pdfCompanyInfo: String, pdfItemTable: String, pdfFooter: String) {
            userSettingDao.updateInvoicePrinterSettings(userId, pdfCompanyInfo, pdfItemTable, pdfFooter)
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


    fun insertPosSetting(posSetting: POSSettings) : Long {
       return userSettingDao.insertPosSetting(posSetting)
    }

    fun getPosSetting(userId: Long): POSSettings? {
        return userSettingDao.getPosSettingsById(userId)
    }

    suspend fun updatePosSetting(userId: Long, isEnableCashBalance: Boolean, isBlockOutOfStock: Boolean) {
            userSettingDao.updatePosSettings(userId, isEnableCashBalance, isBlockOutOfStock)
    }

}