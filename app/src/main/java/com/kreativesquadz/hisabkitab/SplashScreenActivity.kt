package com.kreativesquadz.hisabkitab

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kreativesquadz.hisabkitab.databinding.ActivitySplashScreenBinding
import com.kreativesquadz.hisabkitab.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SplashScreenActivity  : AppCompatActivity() {
    private val viewModel: SplashScreenViewModel by viewModels()
    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel.checkLoginAndLoadData()
        viewModel.navigationState.observe(this) { state ->
            when (state) {
                SplashNavigationState.NavigateToMain -> navigateToMainScreen()
                SplashNavigationState.NavigateToLogin -> navigateToLoginScreen()
            }
        }

    }

    private fun navigateToMainScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateToLoginScreen() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

}