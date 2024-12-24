package com.kreativesquadz.hisabkitab.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import androidx.lifecycle.viewModelScope
import com.kreativesquadz.hisabkitab.repository.LoginRepository

import com.kreativesquadz.hisabkitab.model.UserSession
import com.kreativesquadz.hisabkitab.repository.UserSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(private val loginRepository: LoginRepository,
                                         private val userSessionRepository: UserSessionRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<String>()
    val loginFormState: LiveData<String> = _loginForm
    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult
    val userSession: LiveData<UserSession> = loginRepository.getUserSessionsLiveData()


    fun login(username: String, password: String, isStaff: Boolean) {
        viewModelScope.launch {
            loginRepository.login(username, password,isStaff)
            _loginResult.value = LoginResult(success = "success")
        }
    }


    fun logout() {
        viewModelScope.launch {
            loginRepository.logout()
            _loginResult.value = LoginResult(success = null)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}