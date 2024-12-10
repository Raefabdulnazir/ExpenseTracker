package com.example.expensetracker

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.expensetracker.database.ExpenseDatabase
import com.example.expensetracker.repository.ExpenseRepository
import com.example.expensetracker.repository.IncomeRepository
import com.example.expensetracker.screens.MainScreen
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.example.expensetracker.viewmodel.ExpenseViewModelFactory
import com.example.expensetracker.viewmodel.IncomeViewModel
import com.example.expensetracker.viewmodel.IncomeViewModelFactory
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHost
import com.example.expensetracker.repository.BudgetRepository
import com.example.expensetracker.screens.BudgetPlannerScreen
import com.example.expensetracker.viewmodel.BudgetViewModel
import com.example.expensetracker.viewmodel.BudgetViewModelFactory
import java.util.Calendar

//defining the screens for bottom navigation
sealed class Screen(val route: String,val title:String){
    object Transaction : Screen("transaction","Transaction")
    object Budget : Screen("budget","Budget")
    object Analysis : Screen("analysis","Analysis")
    object Settings : Screen("settings","Settings")
}

class MainActivity : ComponentActivity() {

/*    private val expenseDatabase by lazy { ExpenseDatabase.getDatabase(applicationContext) }

    // Initialize budgetRepository first
    private val budgetRepository by lazy { BudgetRepository(expenseDatabase.budgetDao()) }

    // Initialize expenseRepository next
    private val expenseRepository by lazy { ExpenseRepository(expenseDatabase.expenseDao()) }

    private val incomeRepository by lazy { IncomeRepository(expenseDatabase.incomeDao()) }

    // Initialize budgetViewModel after repositories
    private val budgetViewModel by lazy { BudgetViewModel(budgetRepository, expenseRepository) }*/

    //code 2
    private lateinit var expenseDatabase: ExpenseDatabase
    private lateinit var budgetRepository: BudgetRepository
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var incomeRepository: IncomeRepository
    private lateinit var budgetViewModel: BudgetViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //expenseRepository.budgetViewModel = budgetViewModel//raef test code
        try {
            // Initialize dependencies
            expenseDatabase = ExpenseDatabase.getDatabase(applicationContext)
            budgetRepository = BudgetRepository(expenseDatabase.budgetDao())
            expenseRepository = ExpenseRepository(expenseDatabase.expenseDao())
            incomeRepository = IncomeRepository(expenseDatabase.incomeDao())

            // Initialize ViewModel
            budgetViewModel = BudgetViewModel(budgetRepository, expenseRepository)

            // Resolve dependency
            expenseRepository.budgetViewModel = budgetViewModel

        } catch (e: Exception) {
            Log.e("MainActivity", "Error during initialization: ${e.message}", e)
            return // Stop execution if initialization fails
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

                //Set up the navcontroller for the managing screen transitions
                //navController manages the navigation between different screens in the app and tracks the curretn destinatioon.
                val navController = rememberNavController()

                Scaffold(
                    bottomBar = { BottomNavigationBar(navController)}
                ) {
                    paddingValues ->
                    //Set up navigation graph
                    SetUpNavGraph(
                        navController = navController,
                        incomeViewModel = incomeViewModel,
                        expenseViewModel = expenseViewModel,
                        budgetViewModel = budgetViewModel,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
                        // Apply padding to avoid overlap with system bars if needed
                        /*MainScreen(
                            expenseViewModel = expenseViewModel,
                            incomeViewModel = incomeViewModel,
                            //modifier = Modifier.fillMaxSize().padding(paddingValues)
                        )*/


            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController){
    val items = listOf(
        Screen.Transaction,
        Screen.Budget,
        Screen.Analysis,
        Screen.Settings
    )
    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                selected = navController.currentDestination?.route == screen.route,
                onClick = {
                    navController.navigate(screen.route){
                        //navigating to the destination
                        //ensure pop up and backstack behavior property works properly
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        //make sure only a single instance of the destination exists , avoiding multiple copies when clicking the same tab
                        launchSingleTop = true
                        //restore the state of the previous destination , when navigating back to it
                        restoreState = true

                    }
                          },
                icon = { /*TODO*/ },
                label = { Text(screen.title) }
            )
        }
    }
}

// SetupNavGraph defines the navigation graph, which tells the app how to navigate between screens.
// It defines what to do when a user clicks on a tab, linking the screen's route to the corresponding content.
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetUpNavGraph(navController: NavHostController , incomeViewModel: IncomeViewModel , expenseViewModel: ExpenseViewModel , budgetViewModel: BudgetViewModel , modifier: Modifier = Modifier){
    NavHost(
        navController = navController ,
        startDestination = Screen.Transaction.route ,
        modifier = modifier
    ){
        composable(Screen.Transaction.route) {
            MainScreen(expenseViewModel , incomeViewModel)
        }
        composable(Screen.Budget.route) {
            BudgetPlannerScreen(budgetViewModel)
        }
        composable(Screen.Analysis.route) { 
            Text(text = "Analysis Screen")
        }
        composable(Screen.Settings.route) { 
            Text(text = "Settings Screen")
        }
    }

}

fun getCurrentMonth(): String{
    val calender = Calendar.getInstance()
    val month = calender.get(Calendar.MONTH) + 1//Calendar.MONTH is zero based
    val year = calender.get(Calendar.YEAR)
    return "$year-${month.toString().padStart(2,'0')}"
}