package com.example.financeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.financeapp.data.local.TokenManager
import com.example.financeapp.data.remote.RetrofitClient
import com.example.financeapp.ui.navigation.AppNavigation
import com.example.financeapp.ui.features.auth.AuthViewModel
import com.example.financeapp.ui.features.main.MainViewModel

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        TokenManager.init(applicationContext)
        RetrofitClient.init(applicationContext)

        authViewModel.tryRestoreSession()

        splashScreen.setKeepOnScreenCondition {
            authViewModel.isRestoringSession.value || mainViewModel.isLoading.value
        }
        
        setContent {
            AppNavigation(
                mainViewModel = mainViewModel,
                authViewModel = authViewModel
            )
        }
    }
}
