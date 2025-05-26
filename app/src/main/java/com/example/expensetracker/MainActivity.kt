package com.example.expensetracker

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.expensetracker.database.ExpenseDatabase
import com.example.expensetracker.repository.ExpenseRepository
import com.example.expensetracker.repository.IncomeRepository
import com.example.expensetracker.screens.MainScreen
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.example.expensetracker.viewmodel.ExpenseViewModelFactory
import com.example.expensetracker.viewmodel.IncomeViewModel
import com.example.expensetracker.viewmodel.IncomeViewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.work.WorkManager
import com.example.expensetracker.repository.BudgetRepository
import com.example.expensetracker.repository.CategoryRepository
import com.example.expensetracker.screens.AnalyticsScreen
import com.example.expensetracker.screens.BudgetPlannerScreen
import com.example.expensetracker.screens.CategoryManagementScreen
import com.example.expensetracker.screens.SettingsScreen
import com.example.expensetracker.viewmodel.AnalyticsViewModel
import com.example.expensetracker.viewmodel.AnalyticsViewModelFactory
import com.example.expensetracker.viewmodel.BudgetViewModel
import com.example.expensetracker.viewmodel.BudgetViewModelFactory
import com.example.expensetracker.viewmodel.CategoryViewModel
import com.example.expensetracker.viewmodel.CategoryViewModelFactory
import com.example.expensetracker.work.NotificationScheduler
import java.util.Calendar

// Updated Screen class with icon resources
sealed class Screen(val route: String, val title: String, val iconRes: Int) {
    object Transaction : Screen("transaction", "Records", R.drawable.ic_transaction)
    object Budget : Screen("budget", "Budget", R.drawable.ic_budget)
    object Analysis : Screen("analysis", "Analysis", R.drawable.ic_analysis)
    object Settings : Screen("settings", "Settings", R.drawable.ic_settings)
    object Category : Screen("category", "Category", R.drawable.ic_categories)
}

class MainActivity : ComponentActivity() {

    private lateinit var expenseDatabase: ExpenseDatabase
    private lateinit var budgetRepository: BudgetRepository
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var incomeRepository: IncomeRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var budgetViewModel: BudgetViewModel
    private lateinit var analyticsViewModel: AnalyticsViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
        }

        Log.d("Main activity","Scheduling Notification")
        NotificationScheduler.scheduleDailyNotification(this)

        // For debugging: observe work status
        WorkManager.getInstance(this).getWorkInfosByTagLiveData("daily_notification_tag")
            .observe(this) { workInfoList ->
                Log.d("MainActivity", "Work info list size: ${workInfoList.size}")
                for (workInfo in workInfoList) {
                    Log.d("MainActivity", "Work status: ${workInfo.state}")
                }
            }

        enableEdgeToEdge()

        try {
            // Initialize dependencies
            expenseDatabase = ExpenseDatabase.getDatabase(applicationContext)

            budgetRepository = BudgetRepository(expenseDatabase.budgetDao())
            expenseRepository = ExpenseRepository(expenseDatabase.expenseDao())
            incomeRepository = IncomeRepository(expenseDatabase.incomeDao())
            categoryRepository = CategoryRepository(expenseDatabase.categoryDao())

            // Initialize ViewModel
            budgetViewModel = BudgetViewModel(budgetRepository, expenseRepository)

            // Resolve dependency
            expenseRepository.budgetViewModel = budgetViewModel

        } catch (e: Exception) {
            Log.e("MainActivity", "Error during initialization: ${e.message}", e)
            return
        }

        setContent {
            Log.d("MainActivity", "Inside setContent block")
            ExpenseTrackerTheme {
                Log.d("MainActivity", "Inside ExpenseTrackerTheme block")

                // Provide the viewmodels with their factory
                val expenseViewModel: ExpenseViewModel = viewModel(
                    factory = ExpenseViewModelFactory(expenseRepository)
                )

                val incomeViewModel: IncomeViewModel = viewModel(
                    factory = IncomeViewModelFactory(incomeRepository)
                )

                val budgetViewModel: BudgetViewModel = viewModel(
                    factory = BudgetViewModelFactory(budgetRepository,expenseRepository)
                )

                val analyticsViewModel: AnalyticsViewModel = viewModel(
                    factory = AnalyticsViewModelFactory(expenseRepository,incomeRepository,categoryRepository)
                )

                val categoryViewModel: CategoryViewModel = viewModel(
                    factory = CategoryViewModelFactory(categoryRepository,expenseRepository,incomeRepository,budgetRepository)
                )

                val navController = rememberNavController()

                Scaffold(
                    bottomBar = { BottomNavigationBar(navController) }
                ) { paddingValues ->
                    SetUpNavGraph(
                        navController = navController,
                        incomeViewModel = incomeViewModel,
                        expenseViewModel = expenseViewModel,
                        budgetViewModel = budgetViewModel,
                        analyticsViewModel = analyticsViewModel,
                        categoryViewModel = categoryViewModel,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.Transaction,
        Screen.Budget,
        Screen.Analysis,
        Screen.Category,
        Screen.Settings
    )

    // Get current route to determine selected state
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = Color.White,
        tonalElevation = 8.dp
    ) {
        items.forEach { screen ->
            val isSelected = currentRoute == screen.route

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = screen.iconRes),
                        contentDescription = screen.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF2196F3), // Blue color for selected icon
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = Color(0xFF2196F3), // Blue color for selected text
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color(0xFFE3F2FD) // Light blue background for selected item
                )
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetUpNavGraph(
    navController: NavHostController,
    incomeViewModel: IncomeViewModel,
    expenseViewModel: ExpenseViewModel,
    budgetViewModel: BudgetViewModel,
    analyticsViewModel: AnalyticsViewModel,
    categoryViewModel: CategoryViewModel,
    modifier: Modifier = Modifier
) {
    val month = getCurrentMonth()

    NavHost(
        navController = navController,
        startDestination = Screen.Transaction.route,
        modifier = modifier
    ) {
        composable(Screen.Transaction.route) {
            MainScreen(expenseViewModel, incomeViewModel, categoryViewModel)
        }
        composable(Screen.Budget.route) {
            BudgetPlannerScreen(budgetViewModel, categoryViewModel)
        }
        composable(Screen.Analysis.route) {
            AnalyticsScreen(
                viewModel = analyticsViewModel,
                currentMonth = month
            )
        }
        composable(Screen.Category.route) {
            CategoryManagementScreen(
                viewModel = categoryViewModel
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}

fun getCurrentMonth(): String {
    val calender = Calendar.getInstance()
    val month = calender.get(Calendar.MONTH) + 1
    val year = calender.get(Calendar.YEAR)
    return "$year-${month.toString().padStart(2,'0')}"
}