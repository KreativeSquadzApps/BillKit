package com.kreativesquadz.billkit
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.request.IsUpdated
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import com.kreativesquadz.billkit.repository.InventoryRepository
import com.kreativesquadz.billkit.repository.LoginRepository
import com.kreativesquadz.billkit.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(val settingsRepository: SettingsRepository,
                                                val inventoryRepository: InventoryRepository,
                                                val billHistoryRepository: BillHistoryRepository,
                                                val customerManagRepository: CustomerManagRepository,
                                                val loginRepository: LoginRepository

)
  : ViewModel() {
      var _isDataLoaded : MutableLiveData<IsUpdated> =  MutableLiveData<IsUpdated>()
    val isDataLoaded: LiveData<IsUpdated> = _isDataLoaded
    fun getInitialData() {
        var isLoggedIn = false

        viewModelScope.launch{
            try {
                val loginSession = loginRepository.getUserSessions()
                settingsRepository.loadCompanyDetails(Config.userId)
                inventoryRepository.loadAllProduct(Config.userId)
                inventoryRepository.loadAllCategory(Config.userId)
                delay(2000)
                if (loginSession != null){
                    if (loginSession.staffId != null){
                        loginRepository.getSession(null,loginSession.staffId.toLong())
                        isLoggedIn = true
                        _isDataLoaded.value = IsUpdated(true,isLoggedIn)
                    }else if (loginSession.userId != null){
                        loginRepository.getSession(loginSession.userId,null)
                        isLoggedIn = true
                        _isDataLoaded.value = IsUpdated(true,isLoggedIn)
                    }
                }else{
                    isLoggedIn = false
                    _isDataLoaded.value = IsUpdated(true,false)
                }


            } catch (e: Exception) {
                // Handle any exceptions
                e.printStackTrace()
                _isDataLoaded.value = IsUpdated(false,isLoggedIn)
            }
        }
    }


}