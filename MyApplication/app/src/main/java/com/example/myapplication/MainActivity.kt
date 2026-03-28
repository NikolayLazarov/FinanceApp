package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.InsertChart
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.myapplication.models.CreateExpenseRequest
import com.example.myapplication.models.DummyData
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
import kotlinx.coroutines.launch

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
            val isLoading by mainViewModel.isLoading.collectAsState()
            val expenses by mainViewModel.expenses.collectAsState()
            val timeGroup by mainViewModel.timeGroup.collectAsState()
            val selectedCategory by mainViewModel.selectedCategory.collectAsState()
            val isDarkModeOverride by mainViewModel.isDarkMode.collectAsState()

            // Resolve actual theme: Use override if set, otherwise follow system
            val darkTheme = isDarkModeOverride ?: isSystemInDarkTheme()

            val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
            val isRestoringSession by authViewModel.isRestoringSession.collectAsState()
            val authLoading by authViewModel.isLoading.collectAsState()
            val authError by authViewModel.error.collectAsState()
            val userInfo by authViewModel.userInfo.collectAsState()

            // DEBUG BYPASS
            var bypassAuth by remember { mutableStateOf(false) }

            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    mainViewModel.loadData()
                }
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                if (isRestoringSession) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (!isLoggedIn && !bypassAuth) {
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
                        onClearError = { authViewModel.clearError() },
                        onBypass = { bypassAuth = true }
                    )
                } else if (isLoading && !bypassAuth) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    MyApplicationApp(
                        expenses = if (bypassAuth) DummyData.dummyExpenses else expenses,
                        timeGroup = timeGroup,
                        selectedCategory = selectedCategory,
                        userInfo = userInfo,
                        isDarkMode = darkTheme,
                        onTimeGroupChange = { mainViewModel.setTimeGroup(it) },
                        onCategoryChange = { mainViewModel.setSelectedCategory(it) },
                        onDarkModeChange = { mainViewModel.setDarkMode(it) },
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
                        onLogout = { 
                            if (bypassAuth) bypassAuth = false
                            else authViewModel.logout() 
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MyApplicationApp(
    expenses: List<Product> = emptyList(),
    timeGroup: TimeGroup = TimeGroup.DAY,
    selectedCategory: String? = null,
    userInfo: LoginResult? = null,
    isDarkMode: Boolean = false,
    onTimeGroupChange: (TimeGroup) -> Unit = {},
    onCategoryChange: (String?) -> Unit = {},
    onDarkModeChange: (Boolean?) -> Unit = {},
    onAddExpense: (CreateExpenseRequest) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val pagerState = rememberPagerState(initialPage = AppDestinations.HOME.ordinal) {
        AppDestinations.entries.size
    }
    val scope = rememberCoroutineScope()
    
    var showScanner by remember { mutableStateOf(false) }
    var showAddChooser by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }

    if (showAddChooser) {
        AddChooserDialog(
            onDismiss = { showAddChooser = false },
            onScanReceipt = {
                showAddChooser = false
                showScanner = true
            },
            onAddManually = {
                showAddChooser = false
                showAddExpenseDialog = true
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
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    AppDestinations.entries.forEach { destination ->
                        val selected = destination.ordinal == pagerState.currentPage
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selected) destination.selectedIcon else destination.icon,
                                    contentDescription = destination.label,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    destination.label,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = selected,
                            onClick = { 
                                scope.launch {
                                    pagerState.animateScrollToPage(destination.ordinal)
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            },
            floatingActionButton = {
                if (pagerState.currentPage == AppDestinations.HOME.ordinal) {
                    FloatingActionButton(
                        onClick = { showAddChooser = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 6.dp
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Expense")
                    }
                }
            }
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.padding(innerPadding),
                beyondViewportPageCount = 2
            ) { page ->
                when (AppDestinations.entries[page]) {
                    AppDestinations.STATISTICS -> GraphsPage(
                        expenses = expenses,
                        timeGroup = timeGroup,
                        selectedCategory = selectedCategory,
                        onTimeGroupChange = onTimeGroupChange,
                        onCategoryChange = onCategoryChange
                    )
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
                        isDarkMode = isDarkMode,
                        onDarkModeChange = onDarkModeChange,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

@Composable
private fun AddChooserDialog(
    onDismiss: () -> Unit,
    onScanReceipt: () -> Unit,
    onAddManually: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Expense",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Choose how to add your expense",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onScanReceipt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        Icons.Outlined.DocumentScanner,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan Receipt", fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = onAddManually,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Manually", fontWeight = FontWeight.Medium)
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    STATISTICS("Statistics", Icons.Outlined.InsertChart, Icons.Filled.InsertChart),
    HOME("Home", Icons.Outlined.Home, Icons.Filled.Home),
    PROFILE("Profile", Icons.Outlined.Person, Icons.Filled.Person),
}
