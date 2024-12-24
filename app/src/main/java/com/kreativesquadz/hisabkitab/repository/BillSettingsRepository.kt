package com.kreativesquadz.hisabkitab.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kreativesquadz.hisabkitab.Dao.BillSettingsDao
import com.kreativesquadz.hisabkitab.Database.AppDatabase
import com.kreativesquadz.hisabkitab.api.ApiClient
import com.kreativesquadz.hisabkitab.api.ApiResponse
import com.kreativesquadz.hisabkitab.api.common.NetworkBoundResource
import com.kreativesquadz.hisabkitab.api.common.common.Resource
import com.kreativesquadz.hisabkitab.model.settings.Packaging
import timber.log.Timber
import javax.inject.Inject

class BillSettingsRepository @Inject constructor(val db: AppDatabase){
   private val billSettingsDao : BillSettingsDao = db.billSettingsDao()

   fun loadAllPackagingList(userId : Int): LiveData<Resource<List<Packaging>>> {
      return object : NetworkBoundResource<List<Packaging>, List<Packaging>>() {
         override fun saveCallResult(item: List<Packaging>) {
            try {
               db.runInTransaction {
                  billSettingsDao.deleteAllPackaging()
                  billSettingsDao.insertPackagingList(item)
               }
            } catch (ex: Exception) {
               Timber.tag("Error at loadAllPackagingList()").e(ex.toString())
            }
         }

         override fun shouldFetch(data: List<Packaging>?): Boolean {
            return true
         }

         override fun loadFromDb(): LiveData<List<Packaging>> {
            return billSettingsDao.getPackagingByUserId(userId)
         }

         override fun createCall(): LiveData<ApiResponse<List<Packaging>>> {
            return ApiClient.getApiService().loadAllPackaging(userId)
         }
      }.asLiveData()
   }

   suspend fun addPackaging(packaging: Packaging) : LiveData<Boolean> {
      val statusLiveData = MutableLiveData<Boolean>()
      billSettingsDao.insertPackaging(packaging)
      statusLiveData.value = true
      return statusLiveData
   }

   fun getPackaging(id: Int): Packaging {
      return billSettingsDao.getPackagingById(id)
   }

   suspend fun getUnsyncedPackaging(): List<Packaging> {
      return billSettingsDao.getUnsyncedPackaging()
   }

   suspend fun markPackagingAsSynced(packaging: Packaging) {
      billSettingsDao.update(packaging.copy(isSynced = 1))
   }
   suspend fun updatePackaging(packaging: Packaging) {
      billSettingsDao.update(packaging)
   }

   fun deletePackagingById(id: Int){
      billSettingsDao.deletePackagingById(id)
   }


}