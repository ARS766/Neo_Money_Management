package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.SortOption
import com.example.util.TranslationHelper

sealed class AppStage {
    object Splash : AppStage()
    object Onboarding : AppStage()
    object Auth : AppStage()
    object Main : AppStage()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(
    modifier: Modifier = Modifier,
    financeViewModel: FinanceViewModel = viewModel()
) {
    // Collect Preferences flows
    val isDarkMode by financeViewModel.isDarkMode.collectAsStateWithLifecycle()
    val currency by financeViewModel.selectedCurrency.collectAsStateWithLifecycle()
    val selectedLanguage by financeViewModel.selectedLanguage.collectAsStateWithLifecycle()
    val userPin by financeViewModel.userPin.collectAsStateWithLifecycle()
    val isBiometricEnabled by financeViewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val hasCompletedOnboarding by financeViewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()
    val incomeCategories by financeViewModel.incomeCategories.collectAsStateWithLifecycle()
    val expenseCategories by financeViewModel.expenseCategories.collectAsStateWithLifecycle()
    val isAutoBackupEnabled by financeViewModel.isAutoBackupEnabled.collectAsStateWithLifecycle()

    // Collect DB flows
    val transactions by financeViewModel.allTransactions.collectAsStateWithLifecycle()
    val budgets by financeViewModel.allBudgets.collectAsStateWithLifecycle()
    val goals by financeViewModel.allSavingsGoals.collectAsStateWithLifecycle()

    // Collect Filtered DB Flows
    val filteredTransactions by financeViewModel.filteredTransactions.collectAsStateWithLifecycle()

    // Filter Query States
    val searchQuery by financeViewModel.searchQuery.collectAsStateWithLifecycle()
    val filterCategory by financeViewModel.filterCategory.collectAsStateWithLifecycle()
    val filterType by financeViewModel.filterType.collectAsStateWithLifecycle()
    val sortOption by financeViewModel.sortOption.collectAsStateWithLifecycle()

    // Custom overlay alerts
    val overlayState by financeViewModel.overlayState.collectAsStateWithLifecycle()

    // Screen stages state control
    var currentStage by remember { mutableStateOf<AppStage>(AppStage.Splash) }
    var currentMainScreenRoute by remember { mutableStateOf("dashboard") } // "dashboard", "history", "budget", "savings", "analytics", "settings", "add_transaction"

    // Multi-screen backstack history tracking
    var lastMainScreenRoute by remember { mutableStateOf("dashboard") }

    // Privacy security pause state (blackout split-screen / recents / focus loss)
    val context = LocalContext.current
    val activity = remember(context) {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is android.app.Activity) {
                break
            }
            ctx = ctx.baseContext
        }
        ctx as? android.app.Activity
    }

    var isAppPaused by remember { mutableStateOf(false) }
    var isMultiWindowModeListenerState by remember { mutableStateOf(activity?.isInMultiWindowMode == true) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, activity) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE) {
                isAppPaused = true
            } else if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                isAppPaused = false
            }
            // Real-time security: always inspect multi-window state on any lifecycle transition
            if (activity != null) {
                isMultiWindowModeListenerState = activity.isInMultiWindowMode
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(activity) {
        val componentActivity = activity as? androidx.activity.ComponentActivity
        if (componentActivity == null) {
            onDispose {}
        } else {
            val listener = androidx.core.util.Consumer<androidx.core.app.MultiWindowModeChangedInfo> { info ->
                isMultiWindowModeListenerState = info.isInMultiWindowMode
            }
            componentActivity.addOnMultiWindowModeChangedListener(listener)
            onDispose {
                componentActivity.removeOnMultiWindowModeChangedListener(listener)
            }
        }
    }

    // Force reading of current configuration to trigger immediate recomposition when split window size/orientation changes
    val configuration = LocalConfiguration.current
    val isMultiWindow = (activity?.isInMultiWindowMode == true) || 
                       isMultiWindowModeListenerState || 
                       com.example.MainActivity.isMultiWindowModeRealtime

    val shouldHideContent = isAppPaused || isMultiWindow

    // State for the custom symmetrical bottom-bar with expandable trigger menu
    var isAddMenuExpanded by remember { mutableStateOf(false) }
    var targetTransactionType by remember { mutableStateOf("EXPENSE") }

    // Adaptive sizes detection
    val isExpandedLayout = configuration.screenWidthDp > 600

    MyApplicationTheme(darkTheme = isDarkMode) {
        // Intercept System Back Presses during Main App Stage to prevent accidental app exits from other screens
        if (currentStage == AppStage.Main && currentMainScreenRoute != "dashboard") {
            BackHandler {
                if (currentMainScreenRoute == "add_transaction" || currentMainScreenRoute == "analytics") {
                    currentMainScreenRoute = lastMainScreenRoute
                } else {
                    currentMainScreenRoute = "dashboard"
                }
            }
        }

        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentStage) {
            AppStage.Splash -> {
                SplashScreen(
                    onSplashFinished = {
                        currentStage = when {
                            !hasCompletedOnboarding -> AppStage.Onboarding
                            userPin != null -> AppStage.Auth
                            else -> AppStage.Main
                        }
                    }
                )
            }

            AppStage.Onboarding -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    OnboardingScreen(
                        onOnboardingFinished = {
                            financeViewModel.completeOnboarding()
                            currentStage = if (userPin != null) AppStage.Auth else AppStage.Main
                        },
                        modifier = if (isExpandedLayout) {
                            Modifier
                                .widthIn(max = 500.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(16.dp)
                        } else {
                            Modifier
                        }
                    )
                }
            }

            AppStage.Auth -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoginScreen(
                        savedPin = userPin,
                        isBiometricEnabled = isBiometricEnabled,
                        onLoginSuccess = {
                            currentStage = AppStage.Main
                        },
                        onSetupNewPin = { pin ->
                            financeViewModel.saveUserPin(pin)
                        },
                        modifier = if (isExpandedLayout) {
                            Modifier
                                .widthIn(max = 500.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(16.dp)
                        } else {
                            Modifier
                        }
                    )
                }
            }

            AppStage.Main -> {
                // Main Core Scaffold Layout
                Scaffold(
                    bottomBar = {
                        // Render Custom Nav Bar only on mobile compact layouts (and when not recording transactions)
                        if (!isExpandedLayout && currentMainScreenRoute != "add_transaction") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            ) {
                                // Background bar using Surface
                                Surface(
                                    tonalElevation = 8.dp,
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .navigationBarsPadding()
                                            .height(80.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 1. Home / Dashboard
                                        BottomTabItem(
                                            selected = currentMainScreenRoute == "dashboard" || currentMainScreenRoute == "analytics",
                                            onClick = {
                                                isAddMenuExpanded = false
                                                currentMainScreenRoute = "dashboard"
                                            },
                                            icon = Icons.Filled.Dashboard,
                                            label = "Home",
                                            modifier = Modifier.weight(1f).testTag("nav_home")
                                        )

                                        // 2. Riwayat
                                        BottomTabItem(
                                            selected = currentMainScreenRoute == "history",
                                            onClick = {
                                                isAddMenuExpanded = false
                                                currentMainScreenRoute = "history"
                                            },
                                            icon = Icons.Filled.History,
                                            label = "Riwayat",
                                            modifier = Modifier.weight(1f).testTag("nav_history")
                                        )

                                        // 3. Anggaran
                                        BottomTabItem(
                                            selected = currentMainScreenRoute == "budget",
                                            onClick = {
                                                isAddMenuExpanded = false
                                                currentMainScreenRoute = "budget"
                                            },
                                            icon = Icons.Filled.PieChart,
                                            label = "Anggaran",
                                            modifier = Modifier.weight(1f).testTag("nav_budget")
                                        )

                                        // Symmetrical empty area spacer for centered floating button
                                        Spacer(modifier = Modifier.weight(1.2f))

                                        // 4. Tabungan
                                        BottomTabItem(
                                            selected = currentMainScreenRoute == "savings",
                                            onClick = {
                                                isAddMenuExpanded = false
                                                currentMainScreenRoute = "savings"
                                            },
                                            icon = Icons.Filled.Savings,
                                            label = "Tabungan",
                                            modifier = Modifier.weight(1.5f).testTag("nav_savings")
                                        )

                                        // 5. Setelan
                                        BottomTabItem(
                                            selected = currentMainScreenRoute == "settings",
                                            onClick = {
                                                isAddMenuExpanded = false
                                                currentMainScreenRoute = "settings"
                                            },
                                            icon = Icons.Filled.Settings,
                                            label = "Setelan",
                                            modifier = Modifier.weight(1.5f).testTag("nav_settings")
                                        )
                                    }
                                }

                                // Centered, prominent trigger icon sitting on the top center of the bottom-bar!
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .offset(y = (-16).dp)
                                ) {
                                    Surface(
                                        onClick = { isAddMenuExpanded = !isAddMenuExpanded },
                                        color = if (isAddMenuExpanded) Color(0xFF1E293B) else PrimaryBlue,
                                        contentColor = Color.White,
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = 2.dp,
                                            color = if (isAddMenuExpanded) Color(0xFFEF4444) else Color(0xCC00E5FF)
                                        ),
                                        shadowElevation = 8.dp,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .testTag("expand_add_menu_trigger")
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Icon(
                                                imageVector = if (isAddMenuExpanded) Icons.Filled.Close else Icons.Filled.KeyboardArrowUp,
                                                contentDescription = if (isAddMenuExpanded) "Tutup Menu" else "Buka Menu",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }

                                // Smooth sliding animated extended quick action panel/buttons for "+" transactions, centered above the bar
                                AnimatedVisibility(
                                    visible = isAddMenuExpanded,
                                    enter = androidx.compose.animation.slideInVertically(
                                        animationSpec = androidx.compose.animation.core.tween(300),
                                        initialOffsetY = { it }
                                    ) + fadeIn(animationSpec = androidx.compose.animation.core.tween(300)),
                                    exit = androidx.compose.animation.slideOutVertically(
                                        animationSpec = androidx.compose.animation.core.tween(300),
                                        targetOffsetY = { it }
                                    ) + fadeOut(animationSpec = androidx.compose.animation.core.tween(300)),
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .offset(y = (-72).dp)
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp)
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                        modifier = Modifier
                                            .wrapContentWidth()
                                            .height(54.dp)
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .padding(horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            // Left action: Tambah Pengeluaran
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(Color(0x15EF4444))
                                                    .clickable {
                                                        isAddMenuExpanded = false
                                                        targetTransactionType = "EXPENSE"
                                                        lastMainScreenRoute = currentMainScreenRoute
                                                        currentMainScreenRoute = "add_transaction"
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowDownward,
                                                    contentDescription = "Tambah Pengeluaran",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Pengeluaran",
                                                    color = Color(0xFFEF4444),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            // Divider line in between
                                            VerticalDivider(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .padding(vertical = 12.dp)
                                                    .width(1.dp),
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                            )

                                            // Right action: Tambah Pemasukan
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(Color(0x1510B981))
                                                    .clickable {
                                                        isAddMenuExpanded = false
                                                        targetTransactionType = "INCOME"
                                                        lastMainScreenRoute = currentMainScreenRoute
                                                        currentMainScreenRoute = "add_transaction"
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowUpward,
                                                    contentDescription = "Tambah Pemasukan",
                                                    tint = Color(0xFF10B981),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Pemasukan",
                                                    color = Color(0xFF10B981),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    floatingActionButton = {}
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Render Navigation Rail on Tablet Expanded Sizes
                        if (isExpandedLayout && currentMainScreenRoute != "add_transaction") {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                NavigationRailItem(
                                    selected = currentMainScreenRoute == "dashboard",
                                    onClick = { currentMainScreenRoute = "dashboard" },
                                    icon = { Icon(Icons.Filled.Dashboard, "Home") },
                                    label = { Text("Home") },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = PrimaryBlue,
                                        selectedTextColor = PrimaryBlue,
                                        indicatorColor = Color(0x152563EB)
                                    )
                                )

                                NavigationRailItem(
                                    selected = currentMainScreenRoute == "history",
                                    onClick = { currentMainScreenRoute = "history" },
                                    icon = { Icon(Icons.Filled.History, "Riwayat") },
                                    label = { Text("Riwayat") },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = PrimaryBlue,
                                        selectedTextColor = PrimaryBlue,
                                        indicatorColor = Color(0x152563EB)
                                    )
                                )

                                NavigationRailItem(
                                    selected = currentMainScreenRoute == "budget",
                                    onClick = { currentMainScreenRoute = "budget" },
                                    icon = { Icon(Icons.Filled.PieChart, "Anggaran") },
                                    label = { Text("Anggaran") },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = PrimaryBlue,
                                        selectedTextColor = PrimaryBlue,
                                        indicatorColor = Color(0x152563EB)
                                    )
                                )

                                NavigationRailItem(
                                    selected = currentMainScreenRoute == "savings",
                                    onClick = { currentMainScreenRoute = "savings" },
                                    icon = { Icon(Icons.Filled.Savings, "Tabungan") },
                                    label = { Text("Tabungan") },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = PrimaryBlue,
                                        selectedTextColor = PrimaryBlue,
                                        indicatorColor = Color(0x152563EB)
                                    )
                                )

                                NavigationRailItem(
                                    selected = currentMainScreenRoute == "settings",
                                    onClick = { currentMainScreenRoute = "settings" },
                                    icon = { Icon(Icons.Filled.Settings, "Setelan") },
                                    label = { Text("Setelan") },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = PrimaryBlue,
                                        selectedTextColor = PrimaryBlue,
                                        indicatorColor = Color(0x152563EB)
                                    )
                                )
                            }
                        }

                        // Central main compose content controller with responsive-adaptive widescreen constraints
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(
                                modifier = if (isExpandedLayout) {
                                    Modifier
                                        .fillMaxHeight()
                                        .widthIn(max = 840.dp)
                                        .fillMaxWidth()
                                } else {
                                    Modifier
                                        .fillMaxSize()
                                }
                            ) {
                                when (currentMainScreenRoute) {
                                    "dashboard" -> {
                                        DashboardScreen(
                                            transactions = transactions,
                                            budgets = budgets,
                                            currency = currency,
                                            incomeCategories = incomeCategories,
                                            expenseCategories = expenseCategories,
                                            onUpdateIncomeCategories = { financeViewModel.updateIncomeCategories(it) },
                                            onUpdateExpenseCategories = { financeViewModel.updateExpenseCategories(it) },
                                            onNavigateToAddTransaction = {
                                                lastMainScreenRoute = "dashboard"
                                                currentMainScreenRoute = "add_transaction"
                                            },
                                            onNavigateToBudgetPlanner = {
                                                currentMainScreenRoute = "budget"
                                            },
                                            onNavigateToAnalytics = {
                                                currentMainScreenRoute = "analytics"
                                            },
                                            selectedLanguage = selectedLanguage
                                        )
                                    }

                                    "history" -> {
                                        val combinedCategories = remember(incomeCategories, expenseCategories) {
                                            incomeCategories + expenseCategories
                                        }
                                        HistoryScreen(
                                            transactions = filteredTransactions,
                                            currency = currency,
                                            searchQuery = searchQuery,
                                            onSearchQueryChange = { financeViewModel.searchQuery.value = it },
                                            selectedCategory = filterCategory,
                                            onCategoryChange = { financeViewModel.filterCategory.value = it },
                                            selectedType = filterType,
                                            onTypeChange = { financeViewModel.filterType.value = it },
                                            selectedSort = sortOption,
                                            onSortChange = { financeViewModel.sortOption.value = it },
                                            onDeleteTransaction = { financeViewModel.deleteTransaction(it) },
                                            categories = combinedCategories,
                                            selectedLanguage = selectedLanguage
                                        )
                                    }

                                    "budget" -> {
                                        BudgetPlannerScreen(
                                            budgets = budgets,
                                            transactions = transactions,
                                            currency = currency,
                                            onSaveBudget = { cat, limit, period -> financeViewModel.saveBudget(cat, limit, period) },
                                            onDeleteBudget = { financeViewModel.deleteBudget(it) },
                                            expenseCategories = expenseCategories,
                                            selectedLanguage = selectedLanguage
                                        )
                                    }

                                    "savings" -> {
                                        SavingsGoalScreen(
                                            goals = goals,
                                            currency = currency,
                                            onSaveGoal = { name, target, current, date ->
                                                financeViewModel.saveSavingsGoal(name, target, current, date)
                                            },
                                            onUpdateContribution = { goal, amt ->
                                                financeViewModel.updateSavingsContribution(goal, amt)
                                            },
                                            onDeleteGoal = { financeViewModel.deleteSavingsGoal(it) }
                                        )
                                    }

                                    "analytics" -> {
                                        AnalyticsScreen(
                                            transactions = transactions,
                                            currency = currency
                                        )
                                    }

                                    "settings" -> {
                                        SettingsScreen(
                                            currentCurrency = currency,
                                            onCurrencyChange = { financeViewModel.updateCurrency(it) },
                                            isDarkMode = isDarkMode,
                                            onDarkModeChange = { financeViewModel.updateDarkMode(it) },
                                            savedPin = userPin,
                                            onSavePin = { financeViewModel.saveUserPin(it) },
                                            isBiometricEnabled = isBiometricEnabled,
                                            onBiometricChange = { financeViewModel.updateBiometricState(it) },
                                            onResetData = {
                                                financeViewModel.resetAllData()
                                                currentStage = AppStage.Splash // reboot to start splash
                                            },
                                            transactions = transactions,
                                            budgets = budgets,
                                            goals = goals,
                                            onRestoreBackup = { backupCode, onComplete ->
                                                financeViewModel.restoreBackupCode(backupCode, onComplete)
                                            },
                                            onGenerateBackup = {
                                                financeViewModel.generateBackupCode(transactions, budgets, goals)
                                            },
                                            isAutoBackupEnabled = isAutoBackupEnabled,
                                            onAutoBackupChange = { financeViewModel.updateAutoBackupEnabled(it) },
                                            selectedLanguage = selectedLanguage,
                                            onLanguageChange = { financeViewModel.updateLanguage(it) }
                                        )
                                    }

                                    "add_transaction" -> {
                                        AddTransactionScreen(
                                            currency = currency,
                                            onSaveTransaction = { titleInput, amountInput, dateInput, categoryInput, noteInput, typeInput ->
                                                financeViewModel.insertTransaction(
                                                    titleInput, amountInput, dateInput, categoryInput, noteInput, typeInput
                                                )
                                                currentMainScreenRoute = lastMainScreenRoute // Safe return to caller route
                                            },
                                            onNavigateBack = {
                                                currentMainScreenRoute = lastMainScreenRoute
                                            },
                                            incomeCategories = incomeCategories,
                                            expenseCategories = expenseCategories,
                                            initialType = targetTransactionType
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Custom Overlay feedback notification system panel ---
        // centang putih ditengah lingkaran hijau dengan text "proses berhasil"
        // silang putih ditengah lingkaran merah dengan text "proses gagal"
        AnimatedVisibility(
            visible = overlayState.isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Dialog(onDismissRequest = {}) {
                Box(
                    modifier = Modifier
                        .size(width = 280.dp, height = 240.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFF1E293B))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (overlayState.isSuccess) {
                            // Centang putih di tengah lingkaran hijau
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(Color(0xFF22C55E), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Sukses",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "proses berhasil",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            // Silang putih di tengah lingkaran merah
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(Color(0xFFEF4444), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Gagal",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "proses gagal",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = overlayState.message,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Privacy Blackout Screen overlay
                if (shouldHideContent) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0F172A))
                            .clickable(enabled = false) {},
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = "Privacy Protected",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = TranslationHelper.translate("privacy_locked", selectedLanguage),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = TranslationHelper.translate("privacy_desc", selectedLanguage),
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
}
}

@Composable
fun BottomTabItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .height(30.dp)
                    .width(48.dp)
                    .background(
                        color = if (selected) Color(0x152563EB) else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (selected) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = if (selected) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

