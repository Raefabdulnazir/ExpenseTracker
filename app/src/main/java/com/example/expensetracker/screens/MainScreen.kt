package com.example.expensetracker.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.Income
import com.example.expensetracker.viewmodel.CategoryViewModel
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.example.expensetracker.viewmodel.IncomeViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    expenseViewModel: ExpenseViewModel = viewModel(),
    incomeViewModel: IncomeViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    var currentMonth by remember { mutableStateOf(LocalDate.now()) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var editTransaction by remember { mutableStateOf<Any?>(null) }

    // Observe data
    val incomesState = incomeViewModel.allIncomes.observeAsState()
    val expensesState = expenseViewModel.allExpense.observeAsState()

    // Filter by current month
    val incomes = incomesState.value?.filterByMonth(currentMonth) ?: emptyList()
    val expenses = expensesState.value?.filterByMonth(currentMonth) ?: emptyList()

    // Calculate totals
    val totalIncome = incomes.sumOf { it.amount }
    val totalExpense = expenses.sumOf { it.amount }
    val totalBalance = totalIncome - totalExpense

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

        // Summary Cards Section
        SummarySection(
            totalBalance = totalBalance,
            totalIncome = totalIncome,
            totalExpense = totalExpense
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Transactions Section
        Box(modifier = Modifier.weight(1f)) {
            TransactionsSection(
                incomes = incomes,
                expenses = expenses,
                onEditTransaction = { transaction ->
                    editTransaction = transaction
                    showAddTransactionDialog = true
                },
                onDeleteTransaction = { transaction ->
                    when (transaction) {
                        is Income -> incomeViewModel.delete(transaction)
                        is Expense -> expenseViewModel.delete(transaction)
                    }
                }
            )
        }

        // Add Transaction Button
        Button(
            onClick = {
                editTransaction = null
                showAddTransactionDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Add Transaction",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    // Add/Edit Transaction Dialog
    if (showAddTransactionDialog) {
        AddTransactionDialog(
            transaction = editTransaction,
            onDismiss = {
                showAddTransactionDialog = false
                editTransaction = null
            },
            onSave = { transactionType, category, amount ->
                handleTransactionSave(
                    transactionToEdit = editTransaction,
                    transactionType = transactionType,
                    category = category,
                    amount = amount,
                    incomeViewModel = incomeViewModel,
                    expenseViewModel = expenseViewModel
                )
                showAddTransactionDialog = false
                editTransaction = null
            },
            categoryViewModel = categoryViewModel
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthSelector(
    currentMonth: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Month",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SummarySection(
    totalBalance: Double,
    totalIncome: Double,
    totalExpense: Double
) {
    Column {
        // Total Balance Card
        SummaryCard(
            title = "Total Balance",
            amount = totalBalance,
            backgroundColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Income and Expense Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                title = "Total Income",
                amount = totalIncome,
                backgroundColor = Color(0xFF4CAF50), // Green
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Total Expense",
                amount = totalExpense,
                backgroundColor = Color(0xFFF44336), // Red
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$${"%.2f".format(amount)}",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TransactionsSection(
    incomes: List<Income>,
    expenses: List<Expense>,
    onEditTransaction: (Any) -> Unit,
    onDeleteTransaction: (Any) -> Unit
) {
    if (incomes.isEmpty() && expenses.isEmpty()) {
        EmptyTransactionsState()
    } else {
        GroupedTransactionsList(
            incomes = incomes,
            expenses = expenses,
            onEditTransaction = onEditTransaction,
            onDeleteTransaction = onDeleteTransaction
        )
    }
}

@Composable
private fun EmptyTransactionsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No Transactions Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start tracking your expenses and income by adding your first transaction",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun GroupedTransactionsList(
    incomes: List<Income>,
    expenses: List<Expense>,
    onEditTransaction: (Any) -> Unit,
    onDeleteTransaction: (Any) -> Unit
) {
    // Combine and sort transactions
    val combinedList = (incomes + expenses).sortedByDescending { item ->
        when (item) {
            is Income -> item.date
            is Expense -> item.date
            else -> 0L
        }
    }

    // Group transactions by date
    val groupedTransactions = combinedList.groupBy { transaction ->
        val date = when (transaction) {
            is Income -> transaction.date
            is Expense -> transaction.date
            else -> 0L
        }
        Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    Column {
        Text(
            text = "Recent Transactions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            groupedTransactions.forEach { (date, transactions) ->
                // Date header
                item {
                    DateHeader(date = date)
                }

                // Transactions for this date
                items(transactions) { transaction ->
                    TransactionCard(
                        transaction = transaction,
                        onEdit = { onEditTransaction(transaction) },
                        onDelete = { onDeleteTransaction(transaction) }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DateHeader(date: LocalDate) {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    val headerText = when {
        date.isEqual(today) -> "Today"
        date.isEqual(yesterday) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
    }

    Text(
        text = headerText,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TransactionCard(
    transaction: Any,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val isExpense = transaction is Expense
    val title = if (transaction is Income) transaction.title else (transaction as Expense).title
    val category = if (transaction is Income) transaction.category else (transaction as Expense).category
    val amount = if (transaction is Income) transaction.amount else (transaction as Expense).amount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { showMenu = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Text(
                text = "$${if (isExpense) "-" else "+"}${"%.2f".format(amount)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (isExpense) Color.Red else Color.Green
            )
        }
    }

    // Action Menu Dialog
    if (showMenu) {
        TransactionActionDialog(
            onEdit = {
                onEdit()
                showMenu = false
            },
            onDelete = {
                onDelete()
                showMenu = false
            },
            onDismiss = { showMenu = false }
        )
    }
}

@Composable
private fun TransactionActionDialog(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Transaction Actions",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Edit Transaction")
                }
                Button(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Delete Transaction", color = Color.White)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionDialog(
    transaction: Any? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, Double) -> Unit,
    categoryViewModel: CategoryViewModel
) {
    var transactionType by remember { mutableStateOf("Income") }
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val incomeCategories by categoryViewModel.incomeCategories.collectAsState()
    val expenseCategories by categoryViewModel.expenseCategories.collectAsState()

    // Initialize fields if editing
    LaunchedEffect(transaction) {
        if (transaction != null) {
            when (transaction) {
                is Income -> {
                    transactionType = "Income"
                    category = transaction.category
                    amount = transaction.amount.toString()
                }
                is Expense -> {
                    transactionType = "Expense"
                    category = transaction.category
                    amount = transaction.amount.toString()
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (transaction == null) "Add Transaction" else "Edit Transaction",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Transaction Type Selection
                Text(
                    text = "Transaction Type",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        onClick = {
                            transactionType = "Income"
                            category = ""
                        },
                        label = { Text("Income") },
                        selected = transactionType == "Income",
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        onClick = {
                            transactionType = "Expense"
                            category = ""
                        },
                        label = { Text("Expense") },
                        selected = transactionType == "Expense",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Category Selection
                val categories = if (transactionType == "Income") incomeCategories else expenseCategories

                CategoryDropdown(
                    categories = categories.map { it.name },
                    selectedCategory = category,
                    onCategorySelected = { category = it },
                    label = "Select Category"
                )

                // Amount Input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()
                    when {
                        parsedAmount == null || parsedAmount <= 0 -> {
                            errorMessage = "Please enter a valid amount greater than 0"
                            showErrorDialog = true
                        }
                        category.isEmpty() -> {
                            errorMessage = "Please select a category"
                            showErrorDialog = true
                        }
                        else -> {
                            onSave(transactionType, category, parsedAmount)
                        }
                    }
                }
            ) {
                Text(if (transaction == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )

    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Invalid Input") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun CategoryDropdown(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }
    var displayText by remember { mutableStateOf(if (selectedCategory.isNotEmpty()) selectedCategory else label) }

    LaunchedEffect(selectedCategory) {
        displayText = if (selectedCategory.isNotEmpty()) selectedCategory else label
    }

    Box {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            readOnly = true,
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = if (expanded) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Dropdown"
                    )
                }
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Helper Functions
private fun handleTransactionSave(
    transactionToEdit: Any?,
    transactionType: String,
    category: String,
    amount: Double,
    incomeViewModel: IncomeViewModel,
    expenseViewModel: ExpenseViewModel
) {
    if (transactionToEdit == null) {
        // Adding new transaction
        if (transactionType == "Income") {
            incomeViewModel.insert(
                Income(
                    title = "Income",
                    amount = amount,
                    date = System.currentTimeMillis(),
                    category = category,
                    month = getCurrentMonth()
                )
            )
        } else {
            expenseViewModel.insert(
                Expense(
                    title = "Expense",
                    amount = amount,
                    date = System.currentTimeMillis(),
                    category = category,
                    month = getCurrentMonth()
                )
            )
        }
    } else {
        // Editing existing transaction
        when (transactionToEdit) {
            is Income -> {
                val oldMonth = transactionToEdit.month
                if (transactionType == "Expense") {
                    incomeViewModel.delete(transactionToEdit)
                    expenseViewModel.insert(
                        Expense(
                            title = transactionType,
                            amount = amount,
                            date = System.currentTimeMillis(),
                            category = category,
                            month = oldMonth
                        )
                    )
                } else {
                    incomeViewModel.update(
                        transactionToEdit.copy(
                            title = transactionType,
                            category = category,
                            amount = amount,
                            month = oldMonth
                        )
                    )
                }
            }
            is Expense -> {
                val oldMonth = transactionToEdit.month
                if (transactionType == "Income") {
                    expenseViewModel.delete(transactionToEdit)
                    incomeViewModel.insert(
                        Income(
                            title = transactionType,
                            amount = amount,
                            date = System.currentTimeMillis(),
                            category = category,
                            month = oldMonth
                        )
                    )
                } else {
                    expenseViewModel.update(
                        transactionToEdit.copy(
                            title = transactionType,
                            category = category,
                            amount = amount,
                            month = oldMonth
                        )
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun <T> List<T>.filterByMonth(selectedMonth: LocalDate): List<T> {
    return this.filter {
        val transactionDate = when (it) {
            is Income -> it.date
            is Expense -> it.date
            else -> return@filter false
        }
        val localDate = Instant.ofEpochMilli(transactionDate).atZone(ZoneId.systemDefault()).toLocalDate()
        localDate.month == selectedMonth.month && localDate.year == selectedMonth.year
    }
}