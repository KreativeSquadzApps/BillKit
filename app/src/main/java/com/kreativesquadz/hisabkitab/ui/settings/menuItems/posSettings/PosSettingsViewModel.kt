package com.kreativesquadz.hisabkitab.ui.settings.menuItems.posSettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.model.settings.POSSettings
import com.kreativesquadz.hisabkitab.repository.UserSettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PosSettingsViewModel @Inject constructor(val userSettingRepository: UserSettingRepository)
    : ViewModel() {
    private val _posSettings = MutableStateFlow(POSSettings())
    val posSettings: StateFlow<POSSettings> = _posSettings.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


    private fun insertPosSetting(posSettings: POSSettings) {
        viewModelScope.launch {
            userSettingRepository.insertPosSetting(posSettings)
            _posSettings.value = userSettingRepository.getPosSetting(Config.userId)  ?: POSSettings()
        }
    }

    fun getPosSetting()  {
        if (userSettingRepository.getPosSetting(Config.userId) == null){
            insertPosSetting(POSSettings(userId = Config.userId))
        }
        else{
            _posSettings.value = userSettingRepository.getPosSetting(Config.userId) ?: POSSettings()
        }
    }

    fun isSettingsUpdated(oldSettings: POSSettings, newSettings: POSSettings): Boolean {
        return !oldSettings.isContentEqual(newSettings)
    }


    fun updatePosSettings(newSettings: POSSettings) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Update the repository with the new settings
                userSettingRepository.updatePosSetting(
                    newSettings.userId,
                    newSettings.isEnableCashBalance,
                    newSettings.isBlockOutOfStock
                )
                getPosSetting()
            } catch (e: Exception) {
                // Handle any errors that might occur
                // You could log the error or notify the user
            } finally {
                // Ensure the loading state is set to false even if an error occurs
                _isLoading.value = false
            }
        }
    }
}