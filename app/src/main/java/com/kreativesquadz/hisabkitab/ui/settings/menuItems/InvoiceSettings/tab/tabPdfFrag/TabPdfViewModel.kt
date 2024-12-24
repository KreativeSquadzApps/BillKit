package com.kreativesquadz.hisabkitab.ui.settings.menuItems.InvoiceSettings.tab.tabPdfFrag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.model.settings.PdfSettings
import com.kreativesquadz.hisabkitab.repository.UserSettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TabPdfViewModel @Inject constructor(val userSettingRepository: UserSettingRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _pdfSettings = MutableStateFlow(PdfSettings())
    val pdfSettings: StateFlow<PdfSettings> = _pdfSettings.asStateFlow()

    private val _isExpanded = MutableStateFlow(false)
    val isExpanded: StateFlow<Boolean> get() = _isExpanded


    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }



    fun updateSettings(newSettings: PdfSettings) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                userSettingRepository.updatePdfSetting(
                    newSettings.userId,
                    newSettings.pdfCompanyInfo,
                    newSettings.pdfItemTable,
                    newSettings.pdfColor,
                    newSettings.pdfFooter
                )
                getPdfSetting()
            } catch (e: Exception) {
                // Handle any errors that might occur
                // You could log the error or notify the user
            } finally {
                // Ensure the loading state is set to false even if an error occurs
                _isLoading.value = false
            }
        }
    }

    fun isSettingsUpdated(oldSettings: PdfSettings, newSettings: PdfSettings): Boolean {
        return !oldSettings.isContentEqual(newSettings)
    }

    fun insertPdfSetting(pdfSettings: PdfSettings) {
        viewModelScope.launch {
            userSettingRepository.insertPdfSetting(pdfSettings)
            _pdfSettings.value = userSettingRepository.getPdfSetting(Config.userId)!!
        }
    }
    fun getPdfSetting()  {
        if (userSettingRepository.getPdfSetting(Config.userId) == null){
            insertPdfSetting(PdfSettings(userId = Config.userId))
        }
        else{
            _pdfSettings.value = userSettingRepository.getPdfSetting(Config.userId)!!
        }
    }


    fun setExpandedState(isExpanded: Boolean) {
        viewModelScope.launch {
            _isExpanded.value = isExpanded
        }
    }


}
