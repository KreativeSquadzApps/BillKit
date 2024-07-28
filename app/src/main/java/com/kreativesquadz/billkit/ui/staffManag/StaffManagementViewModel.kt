package com.kreativesquadz.billkit.ui.staffManag

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.Staff
import com.kreativesquadz.billkit.repository.StaffManagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StaffManagementViewModel @Inject constructor(val staffManagRepository: StaffManagRepository) : ViewModel() {
    lateinit var staffList : LiveData<Resource<List<Staff>>>
    fun getStaffList(){
        staffList = staffManagRepository.loadAllStaff(Config.userId)
    }
    
}