package com.kreativesquadz.billkit.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.billkit.repository.LoginRepository
import com.kreativesquadz.billkit.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(val loginRepository: LoginRepository) : ViewModel() {
    fun logout() {
        viewModelScope.launch {
            loginRepository.logout()
        }
    }
    
}