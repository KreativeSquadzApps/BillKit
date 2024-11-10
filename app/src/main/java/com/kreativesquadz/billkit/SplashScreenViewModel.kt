package com.kreativesquadz.billkit
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.api.common.common.Resource
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.InvoicePrefixNumber
import com.kreativesquadz.billkit.model.UserSession
import com.kreativesquadz.billkit.model.request.IsUpdated
import com.kreativesquadz.billkit.model.settings.GST
import com.kreativesquadz.billkit.model.settings.InvoicePrinterSettings
import com.kreativesquadz.billkit.model.settings.PdfSettings
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import com.kreativesquadz.billkit.repository.GstTaxRepository
import com.kreativesquadz.billkit.repository.InventoryRepository
import com.kreativesquadz.billkit.repository.LoginRepository
import com.kreativesquadz.billkit.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
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
