package com.kreativesquadz.hisabkitab.ui.settings.menuItems.printerSettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.model.settings.ThermalPrinterSetup
import com.kreativesquadz.hisabkitab.repository.BluetoothRepository
import com.kreativesquadz.hisabkitab.repository.UserSettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PrinterSettingsViewModel @Inject constructor(val userSettingRepository: UserSettingRepository,
                                                   private val repository: BluetoothRepository
) : ViewModel() {

    private val _printerSettings = MutableStateFlow(ThermalPrinterSetup())
    val printerSettings: StateFlow<ThermalPrinterSetup> = _printerSettings.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


    private fun insertPrinterSetting(thermalPrinterSetup: ThermalPrinterSetup) {
        viewModelScope.launch {
            userSettingRepository.insertPrinterSetting(thermalPrinterSetup)
            _printerSettings.value = userSettingRepository.getPrinterSetting(Config.userId)  ?: ThermalPrinterSetup()
        }
    }

    fun getPrinterSetting()  {
        if (userSettingRepository.getPrinterSetting(Config.userId) == null){
            insertPrinterSetting(ThermalPrinterSetup(userId = Config.userId))
        }
        else{
            _printerSettings.value = userSettingRepository.getPrinterSetting(Config.userId) ?: ThermalPrinterSetup()
        }
    }

    fun isSettingsUpdated(oldSettings: ThermalPrinterSetup, newSettings: ThermalPrinterSetup): Boolean {
        return !oldSettings.isContentEqual(newSettings)
    }


    fun updateSettings(newSettings: ThermalPrinterSetup) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Update the repository with the new settings
                userSettingRepository.updatePrinterSetting(
                    newSettings.userId,
                    newSettings.printerSize,
                    newSettings.printerMode,
                    newSettings.fontSize,
                    newSettings.enableAutoPrint,
                    newSettings.openCashDrawer,
                    newSettings.disconnectAfterPrint,
                    newSettings.autoCutAfterPrint,
                    newSettings.defaultPrinterAddress,
                    newSettings.defaultPrinterName,
                )
                getPrinterSetting()
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