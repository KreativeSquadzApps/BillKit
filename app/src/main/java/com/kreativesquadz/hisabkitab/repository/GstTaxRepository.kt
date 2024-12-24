package com.kreativesquadz.hisabkitab.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kreativesquadz.hisabkitab.Dao.GSTDao
import com.kreativesquadz.hisabkitab.Database.AppDatabase
import com.kreativesquadz.hisabkitab.api.ApiClient
import com.kreativesquadz.hisabkitab.api.ApiResponse
import com.kreativesquadz.hisabkitab.api.common.NetworkBoundResource
import com.kreativesquadz.hisabkitab.api.common.common.Resource
import com.kreativesquadz.hisabkitab.model.settings.GST
import com.kreativesquadz.hisabkitab.model.settings.TaxSettings
import timber.log.Timber
import javax.inject.Inject

class GstTaxRepository @Inject constructor(val db: AppDatabase){
   private val gstTaxDao : GSTDao = db.gstDao()

   fun loadAllgstTax(userId : Int): LiveData<Resource<List<GST>>> {
      return object : NetworkBoundResource<List<GST>, List<GST>>() {
         override fun saveCallResult(item: List<GST>) {
            try {
               db.runInTransaction {
                  gstTaxDao.deleteAllGST()
                  gstTaxDao.insertGstList(item)
                  Timber.d("loadAllCustomers() All Customers loaded ")
               }
            } catch (ex: Exception) {
               Timber.tag("Error at loadAllCustomers()").e(ex.toString())
            }
         }

         override fun shouldFetch(data: List<GST>?): Boolean {
            return true
         }

         override fun loadFromDb(): LiveData<List<GST>> {
            return gstTaxDao.getGSTByUserId(userId)
         }

         override fun createCall(): LiveData<ApiResponse<List<GST>>> {
            return ApiClient.getApiService().loadAllGstTax(userId)
         }
      }.asLiveData()
   }
   suspend fun addGST(gst: GST) : LiveData<Boolean> {
      val statusLiveData = MutableLiveData<Boolean>()
      gstTaxDao.insertGst(gst)
      statusLiveData.value = true
      return statusLiveData
   }

   fun getGST(id: Int): GST {
      return gstTaxDao.getGSTById(id)
   }

   suspend fun getGstByTaxValue(userId: Int, taxValue: Double): GST {
      return gstTaxDao.getGSTByTaxValue( userId, taxValue)
   }
   fun getGSTListByTaxValues(taxAmounts: List<Double>): LiveData<List<GST>> {
      return gstTaxDao.getGSTListByTaxValues(taxAmounts)
   }


   suspend fun getUnsyncedGst(): List<GST> {
      return gstTaxDao.getUnsyncedGST()
   }

   suspend fun markGstAsSynced(gst: GST) {
      gstTaxDao.update(gst.copy(isSynced = 1))
   }
   suspend fun updateGst(gst: GST) {
      gstTaxDao.update(gst)
   }

   fun deleteGSTById(id: Int){
      gstTaxDao.deleteGSTById(id)
   }

   // LiveData to observe changes in tax settings
   fun getTaxSettings(): LiveData<TaxSettings>{
      return gstTaxDao.getTaxSettings(1)
   }
    fun getTaxSettingsObj(): TaxSettings?{
         return gstTaxDao.getTaxSettingsObj(1)
      }

   // Function to save or update the tax settings
   suspend fun saveTaxSettings(taxSettings: TaxSettings) {
      gstTaxDao.insertTaxSettings(taxSettings)
   }

   suspend fun updateTaxSettings(taxSettings: TaxSettings) {
      gstTaxDao.updateTaxSettings(taxSettings)
   }


}