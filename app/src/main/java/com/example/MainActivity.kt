package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.model.TransactionType
import com.example.ui.screens.analytics.AnalyticsScreen
import com.example.ui.screens.analytics.DetailedReportsScreen
import com.example.ui.screens.budgets.BudgetManagementScreen
import com.example.ui.screens.categories.CategoryManagementScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.goals.FinancialGoalsScreen
import com.example.ui.screens.notifications.NotificationsScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.transactions.AddEditTransactionScreen
import com.example.ui.screens.transactions.TransactionDetailScreen
import com.example.ui.screens.transactions.TransactionListScreen
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.viewmodel.ExpenseViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val expenseViewModel: ExpenseViewModel = viewModel()
            val userProfile by expenseViewModel.userProfile.collectAsState()

            val darkTheme = when (userProfile.themeMode.uppercase()) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                MainAppNavHost(
                    viewModel = expenseViewModel,
                    isOnboardingCompleted = userProfile.isOnboardingCompleted
                )
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object MainFlow : Screen("main_flow")
    object AddTransaction : Screen("add_transaction?type={type}&id={id}") {
        fun createRoute(type: TransactionType = TransactionType.EXPENSE, id: Long = -1L): String {
            return "add_transaction?type=${type.name}&id=$id"
        }
    }
    object TransactionDetail : Screen("transaction_detail/{id}") {
        fun createRoute(id: Long): String = "transaction_detail/$id"
    }
    object Categories : Screen("categories")
    object Budgets : Screen("budgets")
    object Goals : Screen("goals")
    object Notifications : Screen("notifications")
    object DetailedReports : Screen("detailed_reports")
}

sealed class BottomTab(
    val route: String,
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
) {
    object Dashboard : BottomTab(
        route = "tab_dashboard",
        title = "Home",
        selectedIcon = Icons.Default.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "bottom_tab_home"
    )
    object Transactions : BottomTab(
        route = "tab_transactions",
        title = "Activity",
        selectedIcon = Icons.Default.ReceiptLong,
        unselectedIcon = Icons.Outlined.ReceiptLong,
        testTag = "bottom_tab_transactions"
    )
    object Analytics : BottomTab(
        route = "tab_analytics",
        title = "Analytics",
        selectedIcon = Icons.Default.BarChart,
        unselectedIcon = Icons.Outlined.BarChart,
        testTag = "bottom_tab_analytics"
    )
    object Profile : BottomTab(
        route = "tab_profile",
        title = "Profile",
        selectedIcon = Icons.Default.Person,
        unselectedIcon = Icons.Outlined.Person,
        testTag = "bottom_tab_profile"
    )
}

@Composable
fun MainAppNavHost(
    viewModel: ExpenseViewModel,
    isOnboardingCompleted: Boolean
) {
    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    if (isOnboardingCompleted) {
                        rootNavController.navigate(Screen.MainFlow.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        rootNavController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onOnboardingFinished = {
                    viewModel.setOnboardingCompleted(true)
                    rootNavController.navigate(Screen.MainFlow.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.MainFlow.route) {
            MainFlowScaffold(
                viewModel = viewModel,
                rootNavController = rootNavController
            )
        }

        composable(
            route = Screen.AddTransaction.route,
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    defaultValue = TransactionType.EXPENSE.name
                },
                navArgument("id") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val typeStr = backStackEntry.arguments?.getString("type") ?: TransactionType.EXPENSE.name
            val initialType = try {
                TransactionType.valueOf(typeStr)
            } catch (e: Exception) {
                TransactionType.EXPENSE
            }
            val id = backStackEntry.arguments?.getLong("id") ?: -1L
            val editId = if (id > 0) id else null

            AddEditTransactionScreen(
                viewModel = viewModel,
                editTransactionId = editId,
                initialType = initialType,
                onNavigateBack = { rootNavController.popBackStack() }
            )
        }

        composable(
            route = Screen.TransactionDetail.route,
            arguments = listOf(
                navArgument("id") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            TransactionDetailScreen(
                transactionId = id,
                viewModel = viewModel,
                onNavigateBack = { rootNavController.popBackStack() },
                onNavigateToEdit = { editId ->
                    rootNavController.navigate(Screen.AddTransaction.createRoute(id = editId))
                }
            )
        }

        composable(Screen.Categories.route) {
            CategoryManagementScreen(
                viewModel = viewModel,
                onNavigateBack = { rootNavController.popBackStack() }
            )
        }

        composable(Screen.Budgets.route) {
            BudgetManagementScreen(
                viewModel = viewModel,
                onNavigateBack = { rootNavController.popBackStack() }
            )
        }

        composable(Screen.Goals.route) {
            FinancialGoalsScreen(
                viewModel = viewModel,
                onNavigateBack = { rootNavController.popBackStack() }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                viewModel = viewModel,
                onNavigateBack = { rootNavController.popBackStack() }
            )
        }

        composable(Screen.DetailedReports.route) {
            DetailedReportsScreen(
                viewModel = viewModel,
                onNavigateBack = { rootNavController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainFlowScaffold(
    viewModel: ExpenseViewModel,
    rootNavController: androidx.navigation.NavController
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Slate200, RoundedCornerShape(24.dp))
                    .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = Slate400.copy(alpha = 0.2f)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left 2 items: Home & Transactions
                    BottomNavTabItem(
                        tab = BottomTab.Dashboard,
                        isSelected = currentRoute == BottomTab.Dashboard.route,
                        onClick = {
                            bottomNavController.navigate(BottomTab.Dashboard.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    BottomNavTabItem(
                        tab = BottomTab.Transactions,
                        isSelected = currentRoute == BottomTab.Transactions.route,
                        onClick = {
                            bottomNavController.navigate(BottomTab.Transactions.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    // Center Primary Add Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Indigo600)
                            .shadow(6.dp, CircleShape, spotColor = Indigo500)
                            .clickable {
                                rootNavController.navigate(Screen.AddTransaction.createRoute(TransactionType.EXPENSE))
                            }
                            .testTag("bottom_bar_add_fab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Transaction",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Right 2 items: Analytics & Profile
                    BottomNavTabItem(
                        tab = BottomTab.Analytics,
                        isSelected = currentRoute == BottomTab.Analytics.route,
                        onClick = {
                            bottomNavController.navigate(BottomTab.Analytics.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    BottomNavTabItem(
                        tab = BottomTab.Profile,
                        isSelected = currentRoute == BottomTab.Profile.route,
                        onClick = {
                            bottomNavController.navigate(BottomTab.Profile.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomTab.Dashboard.route,
            modifier = Modifier.padding(bottom = 0.dp)
        ) {
            composable(BottomTab.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAddTransaction = { type ->
                        rootNavController.navigate(Screen.AddTransaction.createRoute(type))
                    },
                    onNavigateToTransactions = {
                        bottomNavController.navigate(BottomTab.Transactions.route) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToAnalytics = {
                        bottomNavController.navigate(BottomTab.Analytics.route) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToBudgets = {
                        rootNavController.navigate(Screen.Budgets.route)
                    },
                    onNavigateToNotifications = {
                        rootNavController.navigate(Screen.Notifications.route)
                    },
                    onNavigateToTransactionDetail = { id ->
                        rootNavController.navigate(Screen.TransactionDetail.createRoute(id))
                    }
                )
            }

            composable(BottomTab.Transactions.route) {
                TransactionListScreen(
                    viewModel = viewModel,
                    onNavigateToAddTransaction = {
                        rootNavController.navigate(Screen.AddTransaction.createRoute(TransactionType.EXPENSE))
                    },
                    onNavigateToTransactionDetail = { id ->
                        rootNavController.navigate(Screen.TransactionDetail.createRoute(id))
                    }
                )
            }

            composable(BottomTab.Analytics.route) {
                AnalyticsScreen(
                    viewModel = viewModel,
                    onNavigateToDetailedReport = {
                        rootNavController.navigate(Screen.DetailedReports.route)
                    }
                )
            }

            composable(BottomTab.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToCategories = {
                        rootNavController.navigate(Screen.Categories.route)
                    },
                    onNavigateToBudgets = {
                        rootNavController.navigate(Screen.Budgets.route)
                    },
                    onNavigateToGoals = {
                        rootNavController.navigate(Screen.Goals.route)
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavTabItem(
    tab: BottomTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.testTag(tab.testTag)
    ) {
        Icon(
            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
            contentDescription = tab.title,
            tint = if (isSelected) Indigo600 else Slate400,
            modifier = Modifier.size(24.dp)
        )
    }
}
