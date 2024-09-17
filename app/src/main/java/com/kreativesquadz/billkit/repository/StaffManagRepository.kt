package com.kreativesquadz.billkit.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kreativesquadz.billkit.Database.AppDatabase
import com.kreativesquadz.billkit.api.ApiClient
import com.kreativesquadz.billkit.api.ApiResponse
import com.kreativesquadz.billkit.api.common.NetworkBoundResource
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Staff
import javax.inject.Inject

class StaffManagRepository @Inject constructor(private val db: AppDatabase) {
    val staffDao = db.staffDao()

    fun loadAllStaff(adminId:Long): LiveData<Resource<List<Staff>>> {
        return object : NetworkBoundResource<List<Staff>, List<Staff>>() {
            override fun saveCallResult(item: List<Staff>) {

                try {
                    db.runInTransaction {
                        staffDao.deleteStaffList()
                        staffDao.insertStaffList(item)
                        Log.e("44444", item.toString())

                    }
                } catch (ex: Exception) {
                    Log.e("TAG", ex.toString())
                }
            }

            override fun shouldFetch(data: List<Staff>?): Boolean {
                return true
            }

            override fun loadFromDb(): LiveData<List<Staff>> {
                return staffDao.getStaffByUser(adminId)
            }

            override fun createCall(): LiveData<ApiResponse<List<Staff>>> {
                return ApiClient.getApiService().loadStaff(adminId)
            }
        }.asLiveData()
    }

    suspend fun addStaff(Staff: Staff) : LiveData<Boolean> {
        val statusLiveData = MutableLiveData<Boolean>()
        staffDao.insertStaff(Staff)
        statusLiveData.value = true
        return statusLiveData
    }
    suspend fun updateStaff(Staff: Staff) : LiveData<Boolean> {
        val statusLiveData = MutableLiveData<Boolean>()
        staffDao.updateStaff(Staff)
        statusLiveData.value = true
        return statusLiveData
    }


     fun deleteStaff(id: Long) {
        staffDao.deleteStaffById(id)
    }

    suspend fun getUnsyncedStaff(): List<Staff> {
        return staffDao.getUnsyncedStaff()
    }

    suspend fun markstaffAsSynced(Staff: Staff) {
        staffDao.updateStaff(Staff)
    }

    fun getStaffByAdminId(adminId: Long): Staff {
        return staffDao.selectStaffById(adminId)
    }

    fun getStaffById(staffId: Long): Staff {
        return staffDao.selectStaffById(staffId)
    }
}