package com.kreativesquadz.billkit.ui.bluetoothPrinter

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.model.settings.ThermalPrinterSetup
import com.kreativesquadz.billkit.repository.BluetoothRepository
import com.kreativesquadz.billkit.repository.UserSettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothDeviceViewModel @Inject constructor(
    private val repository: BluetoothRepository,
    val userSettingRepository: UserSettingRepository
) : ViewModel() {

    val pairedDevices = MutableLiveData<List<BluetoothDevice>>()
    val isConnected = MutableLiveData<Boolean>()
    val connectionError = MutableLiveData<String>()
    val connectingDevice = MutableLiveData<BluetoothDevice?>()
    val defaultPrinter = MutableLiveData<String?>()

    private val _printerSettings = MutableStateFlow(ThermalPrinterSetup())
    val printerSettings: StateFlow<ThermalPrinterSetup> = _printerSettings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


    // Function to scan paired devices
    fun scanDevices() {
        viewModelScope.launch {
            try {
                pairedDevices.postValue(repository.getPairedDevices())
            } catch (e: SecurityException) {
                connectionError.postValue(e.message)
            }
        }
    }

    // Function to connect to a specific device
    fun connectToDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            connectingDevice.postValue(device)
            try {
                val result = repository.connectToDevice(device.address)
                isConnected.postValue(result)
            } catch (e: SecurityException) {
                connectionError.postValue(e.message)
            } finally {
                connectingDevice.postValue(null)
            }
        }
    }

    // Function to update printer settings
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
                    newSettings.defaultPrinterName
                )
                getPrinterSetting()
            } catch (e: Exception) {
                // Handle any errors that might occur
                connectionError.postValue(e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Function to insert new printer settings if not present
    private fun insertPrinterSetting(thermalPrinterSetup: ThermalPrinterSetup) {
        viewModelScope.launch {
            userSettingRepository.insertPrinterSetting(thermalPrinterSetup)
            _printerSettings.value = userSettingRepository.getPrinterSetting(Config.userId) ?: ThermalPrinterSetup()
        }
    }

    // Function to get the printer settings
    fun getPrinterSetting() {
        viewModelScope.launch {
            val settings = userSettingRepository.getPrinterSetting(Config.userId)
            if (settings == null) {
                insertPrinterSetting(ThermalPrinterSetup(userId = Config.userId))
            } else {
                _printerSettings.value = settings
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Optionally close the connection when the ViewModel is cleared
        repository.closeConnection()
    }
}
