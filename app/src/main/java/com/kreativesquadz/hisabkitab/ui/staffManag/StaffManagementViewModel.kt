package com.kreativesquadz.hisabkitab.ui.staffManag

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.api.common.common.Resource
import com.kreativesquadz.hisabkitab.model.Staff
import com.kreativesquadz.hisabkitab.repository.StaffManagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StaffManagementViewModel @Inject constructor(val staffManagRepository: StaffManagRepository) : ViewModel() {
    lateinit var staffList : LiveData<Resource<List<Staff>>>
    fun getStaffList(){
        staffList = staffManagRepository.loadAllStaff(Config.userId)
    }
    
}