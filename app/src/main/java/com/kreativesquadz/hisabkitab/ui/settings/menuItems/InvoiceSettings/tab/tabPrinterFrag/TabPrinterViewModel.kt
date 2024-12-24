package com.kreativesquadz.hisabkitab.ui.settings.menuItems.InvoiceSettings.tab.tabPrinterFrag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.model.settings.InvoicePrinterSettings
import com.kreativesquadz.hisabkitab.repository.UserSettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TabPrinterViewModel @Inject constructor(val userSettingRepository: UserSettingRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _invoicePrinterSettings = MutableStateFlow(InvoicePrinterSettings())
    val invoicePrinterSettings: StateFlow<InvoicePrinterSettings> = _invoicePrinterSettings.asStateFlow()

    private val _isExpanded = MutableStateFlow(false)
    val isExpanded: StateFlow<Boolean> get() = _isExpanded


    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }



    fun updateInvoicePrinterSettings(newInvoicePrinterSettings: InvoicePrinterSettings) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Update the repository with the new settings
                userSettingRepository.updateInvoicePrinterSetting(
                    newInvoicePrinterSettings.userId,
                    newInvoicePrinterSettings.printerCompanyInfo,
                    newInvoicePrinterSettings.printerItemTable,
                    newInvoicePrinterSettings.printerFooter
                )
                getInvoicePrinterSetting()
            } catch (e: Exception) {
                // Handle any errors that might occur
                // You could log the error or notify the user
            } finally {
                // Ensure the loading state is set to false even if an error occurs
                _isLoading.value = false
            }
        }
    }
    fun isInvoicePrinterSettingsUpdated(oldSettings: InvoicePrinterSettings, newSettings: InvoicePrinterSettings): Boolean {
        return !oldSettings.isContentEqual(newSettings)
    }
    fun insertInvoicePrinterSetting(invoicePrinterSettings: InvoicePrinterSettings) {
        viewModelScope.launch {
            userSettingRepository.insertInvoicePrinterSetting(invoicePrinterSettings)
            _invoicePrinterSettings.value = userSettingRepository.getInvoicePrinterSetting(Config.userId)!!
        }
    }
    fun getInvoicePrinterSetting()  {
        if (userSettingRepository.getPdfSetting(Config.userId) == null){
            insertInvoicePrinterSetting(InvoicePrinterSettings(userId = Config.userId))
        }
        else{
            _invoicePrinterSettings.value = userSettingRepository.getInvoicePrinterSetting(Config.userId)!!
        }
    }


    fun setExpandedState(isExpanded: Boolean) {
        viewModelScope.launch {
            _isExpanded.value = isExpanded
        }
    }


}