package com.kreativesquadz.hisabkitab.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.hisabkitab.repository.LoginRepository
import com.kreativesquadz.hisabkitab.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(val loginRepository: LoginRepository , val settingsRepository: SettingsRepository) : ViewModel() {
    fun logout() {
        viewModelScope.launch {
            loginRepository.logout()
        }
    }
    fun hardReset(userId: Long,context: Context) {
        viewModelScope.launch {
            settingsRepository.hardRest(userId,context)
        }
    }
    
}