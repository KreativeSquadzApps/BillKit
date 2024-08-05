package com.kreativesquadz.billkit.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kreativesquadz.billkit.Dao.GSTDao
import com.kreativesquadz.billkit.Database.AppDatabase
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.api.ApiResponse
import com.kreativesquadz.billkit.api.common.NetworkBoundResource
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.GST
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

   suspend fun getUnsyncedGst(): List<GST> {
      return gstTaxDao.getUnsyncedGST()
   }

   suspend fun markGstAsSynced(gst: GST) {
      gstTaxDao.update(gst.copy(isSynced = 1))
   }



}