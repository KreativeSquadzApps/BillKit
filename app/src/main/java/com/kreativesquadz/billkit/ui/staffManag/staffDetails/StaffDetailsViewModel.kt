package com.kreativesquadz.billkit.ui.staffManag.staffDetails

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kreativesquadz.billkit.model.Staff
import com.kreativesquadz.billkit.repository.StaffManagRepository
import com.kreativesquadz.billkit.worker.DeleteCategoryWorker
import com.kreativesquadz.billkit.worker.DeleteStaffWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaffDetailsViewModel @Inject constructor(var repository: StaffManagRepository) :
    ViewModel() {
        val staffDetails = MutableLiveData<Staff>()

        fun getStaffDetails(id: Long){
            staffDetails.value = repository.getStaffById(id)
        }

    fun deleteStaff(context: Context, id : Long){
        viewModelScope.launch {
            repository.deleteStaff(id)
            deleteStaffWork(context,id.toString())
        }
    }

    private fun deleteStaffWork (context: Context, id: String ) {
        val data = Data.Builder()
            .putString("id",id)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<DeleteStaffWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "deleteStaffWork",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }

}