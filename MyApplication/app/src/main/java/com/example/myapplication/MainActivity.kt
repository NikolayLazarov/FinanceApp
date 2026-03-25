package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.myapplication.models.CreateExpenseRequest
import com.example.myapplication.models.LoginResult
import com.example.myapplication.models.Product
import com.example.myapplication.pages.Scanner
import com.example.myapplication.pages.auth.AuthPage
import com.example.myapplication.pages.main.MainPage
import com.example.myapplication.pages.main.components.AddExpenseDialog
import com.example.myapplication.pages.profile.Profile
import com.example.myapplication.pages.statistics.GraphsPage
import com.example.myapplication.services.RetrofitClient
import com.example.myapplication.services.TokenManager
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.view.AuthViewModel
import com.example.myapplication.view.MainViewModel
import com.example.myapplication.view.TimeGroup

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        TokenManager.init(applicationContext)
        RetrofitClient.init(applicationContext)

        // Try to restore session from saved token
        authViewModel.tryRestoreSession()

        splashScreen.setKeepOnScreenCondition {
            authViewModel.isRestoringSession.value || mainViewModel.isLoading.value
        }

        setContent {
            val isLoading by mainViewModel.isLoading.collectAsState()
            val expenses by mainViewModel.expenses.collectAsState()
            val timeGroup by mainViewModel.timeGroup.collectAsState()
            val selectedCategory by mainViewModel.selectedCategory.collectAsState()

            val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
            val isRestoringSession by authViewModel.isRestoringSession.collectAsState()
            val authLoading by authViewModel.isLoading.collectAsState()
            val authError by authViewModel.error.collectAsState()
            val userInfo by authViewModel.userInfo.collectAsState()

            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    mainViewModel.loadData()
                }
            }

            MyApplicationTheme(darkTheme = false) {
                if (isRestoringSession) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (!isLoggedIn) {
                    AuthPage(
                        isLoading = authLoading,
                        error = authError,
                        onSignIn = { email, password ->
                            authViewModel.signIn(email, password)
                        },
                        onSignUp = { firstName, lastName, age, gender, email, password ->
                            authViewModel.signUp(firstName, lastName, age, gender, email, password) {
                                authViewModel.signIn(email, password)
                            }
                        },
                        onClearError = { authViewModel.clearError() }
                    )
                } else if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    MyApplicationApp(
                        expenses = expenses,
                        timeGroup = timeGroup,
                        selectedCategory = selectedCategory,
                        userInfo = userInfo,
                        onTimeGroupChange = { mainViewModel.setTimeGroup(it) },
                        onCategoryChange = { mainViewModel.setSelectedCategory(it) },
                        onAddExpense = { request ->
                            mainViewModel.addExpense(request) { deductedAmount ->
                                val current = userInfo
                                if (current != null) {
                                    val newAllowance = (current.dailyAllowance - deductedAmount).coerceAtLeast(0.0)
                                    authViewModel.updateAllowance(newAllowance, current.savings)
                                    mainViewModel.updateAllowance(newAllowance, current.savings)
                                }
                            }
                        },
                        onLogout = { authViewModel.logout() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationApp(
    expenses: List<Product> = emptyList(),
    timeGroup: TimeGroup = TimeGroup.DAY,
    selectedCategory: String? = null,
    userInfo: LoginResult? = null,
    onTimeGroupChange: (TimeGroup) -> Unit = {},
    onCategoryChange: (String?) -> Unit = {},
    onAddExpense: (CreateExpenseRequest) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var showScanner by remember { mutableStateOf(false) }
    var showAddChooser by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }

    if (showAddChooser) {
        AlertDialog(
            onDismissRequest = { showAddChooser = false },
            title = { Text("Add Expense") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            showAddChooser = false
                            showScanner = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Scan Receipt")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            showAddChooser = false
                            showAddExpenseDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Manually")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddChooser = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { request ->
                onAddExpense(request)
                showAddExpenseDialog = false
            }
        )
    }

    if (showScanner) {
        Scanner(
            onSave = { _ -> showScanner = false },
            onCancel = { showScanner = false }
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
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = { Text(currentDestination.label) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        actions = {
                            IconButton(onClick = { showAddChooser = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Expense")
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Column(modifier = Modifier.padding(innerPadding)) {
                    when (currentDestination) {
                        AppDestinations.STATISTICS -> GraphsPage()
                        AppDestinations.HOME -> MainPage(
                            expenses = expenses,
                            timeGroup = timeGroup,
                            selectedCategory = selectedCategory,
                            userInfo = userInfo,
                            onTimeGroupChange = onTimeGroupChange,
                            onCategoryChange = onCategoryChange
                        )
                        AppDestinations.PROFILE -> Profile(
                            userInfo = userInfo,
                            onLogout = onLogout
                        )
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
