package com.example.expensetracker.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.getCurrentMonth
import com.example.expensetracker.model.Budget
import com.example.expensetracker.viewmodel.BudgetViewModel
import com.example.expensetracker.viewmodel.CategoryViewModel
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BudgetPlannerScreen(
    budgetViewModel: BudgetViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    var currentMonth by remember { mutableStateOf(LocalDate.now()) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var selectedBudget by remember { mutableStateOf<Budget?>(null) }
    var showEditBudgetDialog by remember { mutableStateOf(false) }

    // Observe data
    val allBudgets by budgetViewModel.allBudgets.collectAsState(initial = emptyList())
    val expenseCategories by categoryViewModel.expenseCategories.collectAsState()

    val budgets = allBudgets.filterByMonthYear(currentMonth)
    val totalBudget = calculateTotalBudget(budgets)
    val totalSpent = calculateTotalSpent(budgets)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Month Selector
        MonthSelector(
            currentMonth = currentMonth,
            onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
            onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Budget Summary Section
        BudgetSummarySection(
            totalBudget = totalBudget,
            totalSpent = totalSpent
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Budget Content
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Budgeted Categories Section
            if (budgets.isNotEmpty()) {
                item {
                    SectionHeaderBudget(title = "Budgeted Categories")
                }

                items(budgets.size) { index ->
                    val budget = budgets[index]
                    MinimalisticBudgetCard(
                        budget = budget,
                        onEditBudget = {
                            selectedBudget = budget
                            showEditBudgetDialog = true
                        },
                        onDeleteBudget = {
                            budgetViewModel.delete(budget)
                        },
                        budgetViewModel = budgetViewModel
                    )
                }
            }

            // Non-budgeted Categories Section
            val expenseCategoryNames = expenseCategories.map { it.name }
            val nonBudgetedCategories = expenseCategoryNames.filter { category ->
                budgets.none { it.categoryName == category }
            }

            if (nonBudgetedCategories.isNotEmpty()) {
                item {
                    SectionHeaderBudget(title = "Available Categories")
                }

                items(nonBudgetedCategories.size) { index ->
                    val category = nonBudgetedCategories[index]
                    val month = getCurrentMonth()
                    val initialSpent by budgetViewModel.fetchInitialSpent(category, month).observeAsState(0.0)

                    NonBudgetedCategoryCard(
                        categoryName = category,
                        onSetBudget = {
                            selectedBudget = Budget(
                                categoryName = category,
                                categoryBudget = 0.0,
                                categorySpent = initialSpent,
                                month = month
                            )
                            showBudgetDialog = true
                        }
                    )
                }
            }

            // Empty State
            if (budgets.isEmpty() && nonBudgetedCategories.isEmpty()) {
                item {
                    EmptyBudgetState()
                }
            }
        }
    }

    // Dialogs
    if (showBudgetDialog && selectedBudget != null) {
        SetBudgetDialog(
            budget = selectedBudget!!,
            onDismiss = { showBudgetDialog = false },
            onBudgetSet = { newBudgetLimit ->
                budgetViewModel.saveOrUpdateBudget(selectedBudget!!.copy(categoryBudget = newBudgetLimit))
                showBudgetDialog = false
            }
        )
    }

    if (showEditBudgetDialog && selectedBudget != null) {
        EditBudgetDialog(
            budget = selectedBudget!!,
            onDismiss = { showEditBudgetDialog = false },
            onBudgetUpdated = { updatedBudget ->
                budgetViewModel.saveOrUpdateBudget(updatedBudget)
                showEditBudgetDialog = false
            }
        )
    }
}

@Composable
private fun BudgetSummarySection(
    totalBudget: Double,
    totalSpent: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Budget",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "$${"%.2f".format(totalBudget)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "$${"%.2f".format(totalSpent)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (totalSpent > totalBudget) Color.Red else Color.Green
                    )
                }
            }

            if (totalBudget > 0) {
                Spacer(modifier = Modifier.height(12.dp))

                val progress = (totalSpent / totalBudget).coerceIn(0.0, 1.0).toFloat()

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = if (progress > 0.8f) Color.Red else MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Remaining: $${"%.2f".format((totalBudget - totalSpent).coerceAtLeast(0.0))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun MinimalisticBudgetCard(
    budget: Budget,
    onEditBudget: () -> Unit,
    onDeleteBudget: () -> Unit,
    budgetViewModel: BudgetViewModel
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val categorySpent by budgetViewModel.getSpentForCategory(budget.categoryName, budget.month).observeAsState(0.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = budget.categoryName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Limit") },
                        onClick = {
                            onEditBudget()
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Budget") },
                        onClick = {
                            onDeleteBudget()
                            menuExpanded = false
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$${"%.2f".format(categorySpent)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = "of $${"%.2f".format(budget.categoryBudget)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progress = if (budget.categoryBudget > 0) {
                (categorySpent / budget.categoryBudget).coerceIn(0.0, 1.0).toFloat()
            } else 0f

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    progress >= 1.0f -> Color.Red
                    progress >= 0.8f -> Color(0xFFFF9800) // Orange
                    else -> Color.Green
                }
            )
        }
    }
}

@Composable
private fun NonBudgetedCategoryCard(
    categoryName: String,
    onSetBudget: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "No budget set",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Button(
                onClick = onSetBudget,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Text("Set Budget")
            }
        }
    }
}

@Composable
private fun EmptyBudgetState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No Budget Categories",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create expense categories first, then set budget limits for them",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SectionHeaderBudget(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SetBudgetDialog(
    budget: Budget,
    onDismiss: () -> Unit,
    onBudgetSet: (Double) -> Unit
) {
    var budgetLimit by remember { mutableStateOf(budget.categoryBudget.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Set Budget",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Category: ${budget.categoryName}",
                    style = MaterialTheme.typography.bodyLarge
                )

                OutlinedTextField(
                    value = budgetLimit,
                    onValueChange = { budgetLimit = it },
                    label = { Text("Budget Limit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val newBudgetLimit = budgetLimit.toDoubleOrNull()
                if (newBudgetLimit != null && newBudgetLimit > 0) {
                    onBudgetSet(newBudgetLimit)
                }
            }) {
                Text("Set Budget")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun EditBudgetDialog(
    budget: Budget,
    onDismiss: () -> Unit,
    onBudgetUpdated: (Budget) -> Unit
) {
    var updatedLimit by remember { mutableStateOf(budget.categoryBudget.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Budget Limit",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = updatedLimit,
                onValueChange = { updatedLimit = it },
                label = { Text("New Budget Limit") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = {
                val newLimit = updatedLimit.toDoubleOrNull()
                if (newLimit != null && newLimit > 0) {
                    onBudgetUpdated(budget.copy(categoryBudget = newLimit))
                }
            }) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

// Helper functions remain the same
fun calculateTotalBudget(budgets: List<Budget>): Double {
    return budgets.sumOf { it.categoryBudget }
}

fun calculateTotalSpent(budgets: List<Budget>): Double {
    return budgets.sumOf { it.categorySpent }
}

fun List<Budget>.filterByMonthYear(monthYear: LocalDate): List<Budget> {
    val selectedMonthYear = monthYear.toString().substring(0, 7)
    return this.filter { budget -> budget.month.startsWith(selectedMonthYear) }
}