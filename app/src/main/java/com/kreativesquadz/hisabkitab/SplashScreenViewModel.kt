package com.kreativesquadz.hisabkitab
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.hisabkitab.repository.BillHistoryRepository
import com.kreativesquadz.hisabkitab.repository.CustomerManagRepository
import com.kreativesquadz.hisabkitab.repository.GstTaxRepository
import com.kreativesquadz.hisabkitab.repository.InventoryRepository
import com.kreativesquadz.hisabkitab.repository.LoginRepository
import com.kreativesquadz.hisabkitab.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val settingsRepository: SettingsRepository,
    private val inventoryRepository: InventoryRepository,
    private val billHistoryRepository: BillHistoryRepository,
    private val customerManagRepository: CustomerManagRepository,
    private val gstTaxRepository: GstTaxRepository
) : ViewModel() {

    // Navigation state
    private val _navigationState = MutableLiveData<SplashNavigationState>()
    val navigationState: LiveData<SplashNavigationState> = _navigationState

    // Data loading states
    private val _isDataLoaded = MutableLiveData<Boolean>()
    val isDataLoaded: LiveData<Boolean> = _isDataLoaded


    // Login check and data loading entry point
    fun checkLoginAndLoadData() {
        viewModelScope.launch {
            val loginSession = loginRepository.getUserSessions()

            if (loginSession != null) {
                // User is logged in, proceed to load data
                _navigationState.value = SplashNavigationState.NavigateToMain
            } else {
                // No session, navigate to login screen
                _navigationState.value = SplashNavigationState.NavigateToLogin
            }
        }
    }

}

// Define navigation states for splash screen
sealed class SplashNavigationState {
    object NavigateToMain : SplashNavigationState()
    object NavigateToLogin : SplashNavigationState()
}
