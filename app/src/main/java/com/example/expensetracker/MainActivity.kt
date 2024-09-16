package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.database.ExpenseDatabase
import com.example.expensetracker.repository.ExpenseRepository
import com.example.expensetracker.repository.IncomeRepository
import com.example.expensetracker.screens.MainScreen
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.example.expensetracker.viewmodel.ExpenseViewModelFactory
import com.example.expensetracker.viewmodel.IncomeViewModel
import com.example.expensetracker.viewmodel.IncomeViewModelFactory


class MainActivity : ComponentActivity() {

    private val expenseDatabase by lazy { ExpenseDatabase.getDatabase(applicationContext) }
    private val expenseRepository by lazy { ExpenseRepository(expenseDatabase.expenseDao()) }

    private val incomeRepository by lazy { IncomeRepository(expenseDatabase.incomeDao()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerTheme {
                //provide the viewmodels with their factory
                val expenseViewModel : ExpenseViewModel = viewModel(
                    factory = ExpenseViewModelFactory(expenseRepository)
                )

                val incomeViewModel : IncomeViewModel = viewModel(
                    factory = IncomeViewModelFactory(incomeRepository)
                )

                    MainScreen(
                        expenseViewModel = expenseViewModel,
                        incomeViewModel = incomeViewModel
                    )
                }
            }
        }
    }

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ExpenseTrackerTheme {
        Greeting("Android")
    }
}