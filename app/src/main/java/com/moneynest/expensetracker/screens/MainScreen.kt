package com.moneynest.expensetracker.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneynest.expensetracker.formatAmount
import com.moneynest.expensetracker.getCurrentMonth
import com.moneynest.expensetracker.model.Expense
import com.moneynest.expensetracker.model.Income
import com.moneynest.expensetracker.utils.SettingsManager
import com.moneynest.expensetracker.viewmodel.CategoryViewModel
import com.moneynest.expensetracker.viewmodel.ExpenseViewModel
import com.moneynest.expensetracker.viewmodel.IncomeViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    expenseViewModel: ExpenseViewModel = viewModel(),
    incomeViewModel: IncomeViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel(),
    settingsManager: SettingsManager
) {
    var currentMonth by remember { mutableStateOf(LocalDate.now()) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var editTransaction by remember { mutableStateOf<Any?>(null) }


    // Observe settings from SettingsManager
    val currencySymbol by settingsManager.currencySymbol.collectAsState()
    val currencyCode by settingsManager.currencyCode.collectAsState()

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

    Box(modifier = Modifier.fillMaxSize()) {

        // Scrollable content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 80.dp) // Space for button
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                // Month Selector
                MonthSelector(
                    currentMonth = currentMonth,
                    onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                    onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                SummarySection(
                    totalBalance = totalBalance,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    currencySymbol = currencySymbol
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Transactions Section
            if (incomes.isEmpty() && expenses.isEmpty()) {
                item {
                    EmptyTransactionsState()
                }
            } else {
                item {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                // Group and display transactions
                val combinedList = (incomes + expenses).sortedByDescending { item ->
                    when (item) {
                        is Income -> item.date
                        is Expense -> item.date
                        else -> 0L
                    }
                }

                val groupedTransactions = combinedList.groupBy { transaction ->
                    val date = when (transaction) {
                        is Income -> transaction.date
                        is Expense -> transaction.date
                        else -> 0L
                    }
                    Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
                }

                groupedTransactions.forEach { (date, transactions) ->
                    item {
                        DateHeader(date = date)
                    }
                    items(transactions) { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            currencySymbol = currencySymbol,
                            onEdit = {
                                editTransaction = transaction
                                showAddTransactionDialog = true
                            },
                            onDelete = {
                                when (transaction) {
                                    is Income -> incomeViewModel.delete(transaction)
                                    is Expense -> expenseViewModel.delete(transaction)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        // Fixed Add Transaction Button at bottom
        Button(
            onClick = {
                editTransaction = null
                showAddTransactionDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Text(
                text = "Add Transaction",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 4.dp)
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
    totalExpense: Double,
    currencySymbol: String,
) {
    Column (verticalArrangement = Arrangement.spacedBy(12.dp)){
        // Total Balance Card
        SummaryCard(
            title = "Total Balance",
            amount = totalBalance,
            currencySymbol = currencySymbol,
            backgroundColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        SummaryCard(
            title = "Total Income",
            amount = totalIncome,
            currencySymbol = currencySymbol,
            backgroundColor = Color(0xFF4CAF50), // Green
            modifier = Modifier.fillMaxWidth()
        )
        SummaryCard(
            title = "Total Expense",
            amount = totalExpense,
            currencySymbol = currencySymbol,
            backgroundColor = Color(0xFFF44336), // Red
            modifier = Modifier.fillMaxWidth()
        )

    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    currencySymbol: String,
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
            Box(    //fixed centered title
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                //text = "$currencySymbol${"%.2f".format(amount)}",
                text = "$currencySymbol${amount.formatAmount()}",
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
    currencySymbol: String,
    onEditTransaction: (Any) -> Unit,
    onDeleteTransaction: (Any) -> Unit
) {
    if (incomes.isEmpty() && expenses.isEmpty()) {
        EmptyTransactionsState()
    } else {
        GroupedTransactionsList(
            incomes = incomes,
            expenses = expenses,
            currencySymbol = currencySymbol,
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
    currencySymbol: String,
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
                        currencySymbol = currencySymbol,
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
    currencySymbol: String,
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
                text = "$currencySymbol${if (isExpense) "-" else "+"}${amount.formatAmount()}",
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

/*@Composable
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
}*/

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

@Composable
private fun CategoryDropdown(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Filter categories based on search query
    val filteredCategories = remember(categories, searchQuery) {
        if (searchQuery.isEmpty()) {
            categories
        } else {
            categories.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    // Reset search when dropdown closes
    LaunchedEffect(expanded) {
        if (!expanded) {
            searchQuery = ""
        }
    }

    Column {
        // Main dropdown trigger
        Box {
            OutlinedTextField(
                value = selectedCategory.ifEmpty { "" },
                onValueChange = {},
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                placeholder = {
                    Text(
                        text = "Select a category",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                readOnly = true,
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                singleLine = true
            )
        }

        // Dropdown content
        if (expanded) {
            Spacer(modifier = Modifier.height(4.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Search field (only show if there are many categories)
                    if (categories.size > 6) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = {
                                Text(
                                    "Search categories",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .padding(horizontal = 8.dp)
                        ) {
                            Divider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                thickness = 1.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Category items
                    if (filteredCategories.isEmpty()) {
                        // No results state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No categories found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                                if (searchQuery.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Try a different search term",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        // Use regular Column with scroll for better compatibility
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            filteredCategories.forEachIndexed { index, category ->
                                val isSelected = category == selectedCategory

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onCategorySelected(category)
                                            expanded = false
                                        },
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                                    } else {
                                        Color.Transparent
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = category,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Check icon for selected item
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                // Subtle divider between items (except for last item)
                                if (index < filteredCategories.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(0.5.dp)
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        Divider(
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                            thickness = 0.5.dp
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
}
