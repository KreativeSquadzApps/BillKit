package com.kreativesquadz.billkit.ui.staffManag.add

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.ApiStatus
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Staff
import com.kreativesquadz.billkit.repository.StaffManagRepository
import com.kreativesquadz.billkit.worker.SyncStaffWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddStaffViewModel @Inject constructor( private val repository: StaffManagRepository) : ViewModel() {
    private var _staffStatus = MutableLiveData<ApiStatus>()
    val staffStatus: LiveData<ApiStatus> get() = _staffStatus

    lateinit var staffList : LiveData<Resource<List<Staff>>>

    fun addStaffObj(context: Context, staff: Staff) {
        if(staff.name.isEmpty()){
            val apiStatus = ApiStatus(400,"Staff Name cannot be empty" )
            _staffStatus.value = apiStatus
            return
        }
        viewModelScope.launch {
            staffList.value?.data?.let {
                it.forEach {
                    if(it.name == staff.name){
                        val apiStatus = ApiStatus(400,"Staff Already Exists" )
                        _staffStatus.value = apiStatus
                        return@launch
                    }
                }
            }
            repository.addStaff(staff)
            scheduleStaffSync(context)
            val apiStatus = ApiStatus(200,"Staff added Successfully" )
            _staffStatus.value = apiStatus
        }
    }

    fun getStaffList(){
        staffList = repository.loadAllStaff(Config.userId)
    }
    fun scheduleStaffSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncStaffWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "staffSyncWork",
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )
    }

}