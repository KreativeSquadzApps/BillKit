package com.kreativesquadz.billkit.ui.settings.menuItems.InvoiceSettings.tab.tabInvoiceFrag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TabInvoiceViewModel @Inject constructor(val settingsRepository: SettingsRepository,
   var billHistoryRepository: BillHistoryRepository) : ViewModel() {
    private val _updateCompanyDetailsStatus = MutableLiveData<Boolean>()
    val updateCompanyDetailsStatus: LiveData<Boolean> get() = _updateCompanyDetailsStatus
    lateinit var companyDetails : LiveData<Resource<CompanyDetails>>


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


}