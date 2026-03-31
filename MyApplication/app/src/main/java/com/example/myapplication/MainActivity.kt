package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.myapplication.services.RetrofitClient
import com.example.myapplication.services.TokenManager
import com.example.myapplication.ui.AppNavigation
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.MainViewModel

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
