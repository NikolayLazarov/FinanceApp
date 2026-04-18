package com.example.financeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.financeapp.data.local.TokenManager
import com.example.financeapp.data.remote.RetrofitClient
import com.example.financeapp.ui.navigation.AppNavigation
import com.example.financeapp.ui.features.auth.AuthViewModel
import com.example.financeapp.ui.features.main.MainViewModel

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        TokenManager.init(applicationContext)
        RetrofitClient.init(applicationContext)

        mainViewModel.loadLanguage()
        authViewModel.tryRestoreSession()

        setContent {
            AppNavigation(
                mainViewModel = mainViewModel,
                authViewModel = authViewModel
            )
        }
    }
}
