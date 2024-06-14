package com.kreativesquadz.billkit.ui.settings.menuItems.InvoiceSettings.tab.tabInvoiceFrag

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
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.UserSetting
import com.kreativesquadz.billkit.repository.SettingsRepository
import com.kreativesquadz.billkit.repository.UserSettingRepository
import com.kreativesquadz.billkit.worker.SyncUserSettingWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TabInvoiceViewModel @Inject constructor(val settingsRepository: SettingsRepository, val userSettingRepository: UserSettingRepository)
    : ViewModel() {
    private val _updateCompanyDetailsStatus = MutableLiveData<Boolean>()
    lateinit var companyDetails : LiveData<Resource<CompanyDetails>>
    private val _updateUsersettinStatus = MutableLiveData<Boolean>()
    lateinit var userSetting : LiveData<UserSetting>


    fun getCompanyDetailsTab(): LiveData<Resource<CompanyDetails>> {
        companyDetails = settingsRepository.loadCompanyDetails(Config.userId)
        return companyDetails
    }


    fun putCompanyObjDetails(companyDetails: CompanyDetails?) {
        viewModelScope.launch {
            val result = settingsRepository.updateCompanyDetails(companyDetails)
            _updateCompanyDetailsStatus.value = result.value
        }
    }



    fun getUserSettings(): LiveData<UserSetting> {
        userSetting = userSettingRepository.getUserSetting(Config.userId)
        return userSetting
    }

    fun updateDiscount(context: Context,userSetting: UserSetting,isUpdate:Boolean){
        userSettingRepository.insert(userSetting)
        _updateUsersettinStatus.value = isUpdate
            //scheduleSettingSync(context)
    }

   private fun scheduleSettingSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncUserSettingWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "settingSyncWork",
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )
    }



}