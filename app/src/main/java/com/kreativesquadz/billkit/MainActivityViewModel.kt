package com.kreativesquadz.billkit
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.LoginResponse
import com.kreativesquadz.billkit.model.UserSession
import com.kreativesquadz.billkit.model.request.IsUpdated
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import com.kreativesquadz.billkit.repository.InventoryRepository
import com.kreativesquadz.billkit.repository.LoginRepository
import com.kreativesquadz.billkit.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(val settingsRepository: SettingsRepository,
                                                val inventoryRepository: InventoryRepository,
                                                val billHistoryRepository: BillHistoryRepository,
                                                val customerManagRepository: CustomerManagRepository,
                                                val loginRepository: LoginRepository
)
  : ViewModel() {

    lateinit var companyDetailsMain : LiveData<Resource<CompanyDetails>>
    val _loginResponse = MutableLiveData<LoginResponse>()
     val loginResponse : LiveData<LoginResponse> = _loginResponse

    fun getCompanyDetails() : LiveData<Resource<CompanyDetails>> {
        viewModelScope.launch {
            companyDetailsMain = settingsRepository.loadCompanyDetails(Config.userId)
        }
        return companyDetailsMain
    }

    fun getSesssion() {
           val loginSession = loginRepository.getUserSessions()
            if (loginSession != null){
                if (loginSession.staffId != null){
                    _loginResponse.value = loginRepository.getSession(null, loginSession.staffId.toLong())
                }else if (loginSession.userId != null){
                    _loginResponse.value = loginRepository.getSession(loginSession.userId, null)
                }
            }else{
                _loginResponse.value = loginRepository.getSession(null, null)
            }


    }
  }