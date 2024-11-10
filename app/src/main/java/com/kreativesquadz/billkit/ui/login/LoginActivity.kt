package com.kreativesquadz.billkit.ui.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import com.kreativesquadz.billkit.MainActivity
import com.kreativesquadz.billkit.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    val loginViewModel: LoginViewModel by viewModels()
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.isLoginStaff = false
        observers()
        onClickListeners()

    }

    fun observers(){
        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

        })

        loginViewModel.loginResult.observe(this@LoginActivity) {
            binding.loading.visibility = View.GONE
            if (it.error != null) {
                showLoginFailed(it.error)
            }
            if (it.success != null) {
                updateUiWithUser()
                setResult(Activity.RESULT_OK)

            }
        }
    }


    private fun updateUiWithUser(){
        loginViewModel.userSession.observe(this@LoginActivity) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    private fun onClickListeners() {
        binding.txtStaffLogin.setOnClickListener {
            binding.isLoginStaff = true
        }
        binding.txtBack.setOnClickListener {
            binding.isLoginStaff = false
        }

        binding.login.setOnClickListener {
            binding.loading.visibility = View.VISIBLE
            loginViewModel.login(binding.username.text.toString(), binding.password.text.toString(), false)
        }
        binding.staffLogin.setOnClickListener {
            binding.loading.visibility = View.VISIBLE
            loginViewModel.login(binding.staffUsername.text.toString(), binding.staffPassword.text.toString(), true)
        }
    }
}