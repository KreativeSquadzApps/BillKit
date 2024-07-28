package com.kreativesquadz.billkit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kreativesquadz.billkit.databinding.ActivitySplashScreenBinding
import com.kreativesquadz.billkit.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SplashScreenActivity  : AppCompatActivity() {
    private val viewModel: SplashScreenViewModel by viewModels()
    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel.getInitialData()
    }

    override fun onResume() {
        super.onResume()
        viewModel.isDataLoaded.observe(this) { isLoaded ->
            if (isLoaded.isDataLoaded) {
                if (isLoaded.isLoggedIn){
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else{
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

        }
    }

}