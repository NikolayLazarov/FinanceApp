package com.example.financeapp.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financeapp.data.model.*
import com.example.financeapp.ui.features.auth.AuthPage
import com.example.financeapp.ui.features.auth.AuthViewModel
import com.example.financeapp.ui.features.main.MainPage
import com.example.financeapp.ui.features.main.MainViewModel
import com.example.financeapp.ui.features.main.components.AddExpenseDialog
import com.example.financeapp.ui.features.profile.Profile
import com.example.financeapp.ui.features.statistics.GraphsPage
import com.example.financeapp.ui.features.scanner.Scanner
import com.example.financeapp.ui.theme.FinanceAppTheme
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    mainViewModel: MainViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val isLoading by mainViewModel.isLoading.collectAsState()
    val expenses by mainViewModel.expenses.collectAsState()
    val timeGroup by mainViewModel.timeGroup.collectAsState()
    val selectedCategory by mainViewModel.selectedCategory.collectAsState()
    val isDarkModeOverride by mainViewModel.isDarkMode.collectAsState()

    val darkTheme = isDarkModeOverride ?: isSystemInDarkTheme()

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isRestoringSession by authViewModel.isRestoringSession.collectAsState()
    val authLoading by authViewModel.isLoading.collectAsState()
    val authError by authViewModel.error.collectAsState()
    val userInfo by authViewModel.userInfo.collectAsState()

    var bypassAuth by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            mainViewModel.loadData()
        }
    }

    FinanceAppTheme(darkTheme = darkTheme) {
        if (isRestoringSession) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (!isLoggedIn && !bypassAuth) {
            AuthPage(
                isLoading = authLoading,
                error = authError,
                onSignIn = { email, password -> authViewModel.signIn(email, password) },
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
                            val newAllowance = current.dailyAllowance - deductedAmount
                            authViewModel.updateAllowance(newAllowance, current.savings)
                            mainViewModel.updateAllowance(newAllowance, current.savings)
                        }
                    }
                },
                onUpdateExpense = { mainViewModel.updateExpense(it) },
                onDeleteExpense = { mainViewModel.deleteExpense(it) },
                onUpdateBudgetAndSavings = { newBudget, newSavings ->
                    authViewModel.updateAllowance(newBudget, newSavings)
                    mainViewModel.updateAllowance(newBudget, newSavings)
                },
                onLogout = {
                    if (bypassAuth) bypassAuth = false
                    else authViewModel.logout()
                }
            )
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
    onUpdateExpense: (CreateExpenseRequest) -> Unit = {},
    onDeleteExpense: (Int) -> Unit = {},
    onUpdateBudgetAndSavings: (Double, Double) -> Unit = { _, _ -> },
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
            },
            onConfirmMultiple = { requests ->
                requests.forEach { onAddExpense(it) }
                showAddExpenseDialog = false
            }
        )
    }

    if (showScanner) {
        Scanner(
            onSave = { expenses ->
                expenses.forEach { onAddExpense(it) }
                showScanner = false
            },
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
                        onCategoryChange = onCategoryChange,
                        onUpdateExpense = onUpdateExpense,
                        onDeleteExpense = onDeleteExpense
                    )
                    AppDestinations.PROFILE -> Profile(
                        userInfo = userInfo,
                        isDarkMode = isDarkMode,
                        onDarkModeChange = onDarkModeChange,
                        onUpdateBudgetAndSavings = onUpdateBudgetAndSavings,
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
