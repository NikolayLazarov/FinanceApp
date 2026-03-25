package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.myapplication.models.Product
import com.example.myapplication.models.ScannedDocument
import com.example.myapplication.pages.Scanner
import com.example.myapplication.pages.main.MainPage
import com.example.myapplication.pages.profile.Profile
import com.example.myapplication.pages.statistics.GraphsPage
import com.example.myapplication.ui.theme.MyApplicationTheme
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
            val isLoading by viewModel.isLoading.collectAsState()
            val products by viewModel.products.collectAsState()
            val scannedDocuments by viewModel.scannedDocuments.collectAsState()

            MyApplicationTheme(darkTheme = false) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    MyApplicationApp(
                        products = products,
                        scannedDocuments = scannedDocuments,
                        onAddScannedDocuments = { docs -> viewModel.addScannedDocuments(docs) }
                    )
                }
            }
        }
    }
}

@Composable
fun MyApplicationApp(
    products: List<Product> = emptyList(),
    scannedDocuments: List<ScannedDocument> = emptyList(),
    onAddScannedDocuments: (List<ScannedDocument>) -> Unit = {}
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var showScanner by remember { mutableStateOf(false) }

    if (showScanner) {
        Scanner(
            onSave = { docs ->
                onAddScannedDocuments(docs)
                showScanner = false
            },
            onCancel = {
                showScanner = false
            }
        )
    } else {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach { destination ->
                    item(
                        icon = {
                            Icon(
                                destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = { Text(destination.label) },
                        selected = destination == currentDestination,
                        onClick = { currentDestination = destination }
                    )
                }
            }
        ) {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Column(modifier = Modifier.padding(innerPadding)) {
                    when (currentDestination) {
                        AppDestinations.STATISTICS -> GraphsPage()
                        AppDestinations.HOME -> MainPage(
                            products = products,
                            scannedDocuments = scannedDocuments,
                            onNewScanner = { showScanner = true }
                        )
                        AppDestinations.PROFILE -> Profile()
                    }
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
