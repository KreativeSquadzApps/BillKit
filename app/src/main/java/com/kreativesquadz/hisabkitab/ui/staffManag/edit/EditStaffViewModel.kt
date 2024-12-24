package com.kreativesquadz.hisabkitab.ui.staffManag.edit

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kreativesquadz.hisabkitab.api.ApiStatus
import com.kreativesquadz.hisabkitab.model.Staff
import com.kreativesquadz.hisabkitab.repository.StaffManagRepository
import com.kreativesquadz.hisabkitab.worker.UpdaterStaffDetailsWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditStaffViewModel @Inject constructor(private val repository: StaffManagRepository) : ViewModel() {
    private var _staffStatus = MutableLiveData<ApiStatus>()
    val staffStatus: LiveData<ApiStatus> get() = _staffStatus



    fun addStaffObj(context: Context, staff: Staff) {
        if(staff.name.isEmpty()){
            val apiStatus = ApiStatus(400,"Staff Name cannot be empty" )
            _staffStatus.value = apiStatus
            return
        }
        viewModelScope.launch {
            repository.updateStaff(staff)
            scheduleStaffDetailsUpdate(context , staff.id.toString())
            val apiStatus = ApiStatus(200,"Staff Updated Successfully" )
            _staffStatus.value = apiStatus
        }
    }

    fun scheduleStaffDetailsUpdate(context: Context, id: String) {
        val data = Data.Builder()
            .putString("id",id)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<UpdaterStaffDetailsWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "scheduleStaffDetailsUpdate",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }

}