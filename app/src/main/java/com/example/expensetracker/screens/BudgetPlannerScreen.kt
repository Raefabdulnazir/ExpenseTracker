package com.example.expensetracker.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.model.Budget
import com.example.expensetracker.viewmodel.BudgetViewModel
import org.w3c.dom.Text
import java.lang.reflect.Modifier
import java.time.LocalDate
import android.util.Log//Import Log class

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BudgetPlannerScreen(budgetViewModel: BudgetViewModel = viewModel()){

    var showBudgetDialog by remember { mutableStateOf(false) }
    var selectedBudget by remember { mutableStateOf<Budget?>(null) }

    Column(modifier = androidx.compose.ui.Modifier
        .fillMaxSize()
        .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally)
    {

        var currentMonth by remember {
            mutableStateOf(LocalDate.now())
        }

        val allBudgets by budgetViewModel.allBudgets.observeAsState(emptyList())
        val budgets = allBudgets.filterByMonthYear(currentMonth)
        Log.d("UI-Budgets", "Current budgets in UI: $budgets")

        //test code - to check whether the list of budget is being displayed in UI
/*        val allBudgets by budgetViewModel.allBudgets.observeAsState(emptyList())
        allBudgets.forEach { budget ->
            Text(text = "${budget.categoryName}: ${budget.categoryBudget}")
        }*/

        monthSelector(
            currentMonth = currentMonth,
            onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
            onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
        )

        val totalBudget = calculateTotalBudget(budgets)
        val totalSpent = calculateTotalSpent(budgets)

        //one row for income box and expense box
        Row(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly //to spread evenly
        ) {
            SummaryCard(
                title = "Total Budget",
                amount = totalBudget,
                backgroundColor = Color(0xFF8BC34A), // Light Green
                modifier = androidx.compose.ui.Modifier.weight(1f)// Occupy half of the width)
            )
            SummaryCard(
                title = "Total Spent",
                amount = totalSpent,
                backgroundColor = Color(0xFFFFCDD2),// Light Red
                modifier = androidx.compose.ui.Modifier.weight(1f)//Occupy half of the width
            )
        }

        LazyColumn(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            //budgeted categories
            item {
                Text(
                    text = "Budgeted categories",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = androidx.compose.ui.Modifier.padding(bottom = 8.dp)
                )
            }

            if (budgets.isEmpty()) {
                item {
                    Text(
                        text = "There's no budget set for this month yet. You can add your budget limits for this month or copy them from previous months",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Light,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = androidx.compose.ui.Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            } else {
                items(budgets.size) { index ->
                    val budget = budgets[index]
                    BudgetItem(budget = budget, onSetBudgetClick = {
                        selectedBudget = budget
                        showBudgetDialog = true
                    })
                }
            }


            //non budgeted categories
            item {
                Text(
                    text = "Non Budgeted categories",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = androidx.compose.ui.Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            //predefined categories for the budget
            val predefinedCategories = listOf(
                "Food",
                "Groceries",
                "Entertainment",
                "Transportation",
                "Utilities",
                "Other"
            )
            val nonBudgetedCategories = predefinedCategories.filter { category ->
                budgets.none { it.categoryName == category }
            }

            items(nonBudgetedCategories.size) { index ->// items() expects an Int for the size of the list
                val category = nonBudgetedCategories[index]// Get the budget object from the list
                //val budget = budgets.find { it.categoryName == category } ?: Budget(categoryName = category, categoryBudget = 0.0, categorySpent = 0.0, month = currentMonth.toString())
                Row(
                    modifier = androidx.compose.ui.Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween    //Align a row and button in a row
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 16.sp
                    )
                    Button(onClick = {
                        selectedBudget = Budget(
                            categoryName = category,
                            categoryBudget = 0.0,
                            categorySpent = 0.0,
                            month = currentMonth.toString()
                        )
                        showBudgetDialog = true
                    }) {
                        Text("Set BUDGET")
                    }
                }
            }
        }
    }

            //display the dialogue when the state is set to true
            if (showBudgetDialog && selectedBudget != null) {
                setBudgetDialogue(
                    budget = selectedBudget!!,
                    onDismissRequest = { showBudgetDialog = false },
                    onBudgetSet = { newBudgetLimit ->
                        budgetViewModel.saveOrUpdateBudget(selectedBudget!!.copy(categoryBudget = newBudgetLimit))
                        showBudgetDialog = false
                    }
                )
            }
    }

@Composable
fun BudgetItem(budget: Budget,onSetBudgetClick:() -> Unit){
    Card(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row {
                Text(text = budget.categoryName, style = MaterialTheme.typography.bodyMedium)
                Text(text = "Limit : ${budget.categoryBudget}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Spent : ${budget.categorySpent}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Remaining : ${budget.categoryBudget - budget.categorySpent}", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = (budget.categorySpent/budget.categoryBudget).toFloat(),
                    onValueChange = {},
                    valueRange = 0f..1f,
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth())

                Button(
                    onClick = onSetBudgetClick,
                    modifier = androidx.compose.ui.Modifier.padding(start = 8.dp)//padding for space between text and button
                ) {
                    Text("Set Budget")
                }
            }

        }
    }
}

fun calculateTotalBudget(budgets:List<Budget>): Double {
    return budgets.sumOf { it.categoryBudget }
}

fun calculateTotalSpent(budgets:List<Budget>): Double {
    return budgets.sumOf { it.categorySpent }
}

@Composable
fun setBudgetDialogue(
    budget: Budget,
    onDismissRequest: () -> Unit,
    onBudgetSet: (Double) -> Unit
){
    var budgetLimit by remember {
        mutableStateOf(budget.categoryBudget.toString())
    }

    AlertDialog(
        onDismissRequest = { onDismissRequest },
        title = {
            Text(text = "Set BUDGET", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = androidx.compose.ui.Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Category: ${budget.categoryName}", style = MaterialTheme.typography.bodyLarge)

                //Budget Limit Field
                OutlinedTextField(
                    value = budgetLimit,
                    onValueChange = {budgetLimit = it},
                    label = {Text("Limit")},
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = { 
            Button(onClick = {
                val newBudgetLimit = budgetLimit.toDoubleOrNull()
                if (newBudgetLimit != null){
                    onBudgetSet(newBudgetLimit)
                    }
                onDismissRequest()
                }
            ) {
                Text("Set")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

fun List<Budget>.filterByMonthYear(monthYear: LocalDate): List<Budget> {
    val selectedMonthYear = monthYear.toString().substring(0, 7)  // Format to "YYYY-MM"
    return this.filter { budget -> budget.month.startsWith(selectedMonthYear) }
}