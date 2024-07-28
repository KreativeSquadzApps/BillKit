package com.kreativesquadz.billkit.ui.staffManag.staffDetails

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kreativesquadz.billkit.model.Staff
import com.kreativesquadz.billkit.repository.StaffManagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StaffDetailsViewModel @Inject constructor(var repository: StaffManagRepository) :
    ViewModel() {
        val staffDetails = MutableLiveData<Staff>()

        fun getStaffDetails(id: Long){
            staffDetails.value = repository.getStaffById(id)
        }
}