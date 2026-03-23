package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.compose.MyApplicationTheme
import com.example.myapplication.pages.main.MainPage
import com.example.myapplication.models.Product
import com.example.myapplication.pages.statistics.GraphsPage
import com.example.myapplication.pages.profile.Profile
import com.example.myapplication.services.RetrofitClient
import com.example.myapplication.view.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition {
            viewModel.isLoading.value
        }
        setContent {
            // 1. Създаваме състояние за нашия списък
            var products by remember { mutableStateOf<List<Product>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                try {
                    products = RetrofitClient.apiService.getProducts()
                } catch (e: Exception) {
                    // Тук може да добавите логика за грешки
                } finally {
                    isLoading = false
                }
            }


            if (isLoading) {
                CircularProgressIndicator() // Индикатор за зареждане
            } else {
                MyApplicationTheme(darkTheme = false, content = { MyApplicationApp(products) })
            }

        }
    }
}

@PreviewScreenSizes
@Composable
fun MyApplicationApp(products: List<Product> = emptyList() ) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column() {
                when (currentDestination){
                    AppDestinations.STATISTICS -> GraphsPage(modifier = Modifier.padding(innerPadding))
                    AppDestinations.HOME -> MainPage(products = products, modifier = Modifier.padding(innerPadding))
                    AppDestinations.PROFILE -> Profile(modifier = Modifier.padding(innerPadding) )

                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    STATISTICS("Statistics", Icons.Default.Info),
    HOME("Home", Icons.Default.Home),
    PROFILE("Profile", Icons.Default.AccountBox),
}
