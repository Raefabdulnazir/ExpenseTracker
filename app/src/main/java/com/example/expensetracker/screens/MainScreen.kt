package com.example.expensetracker.screens

import android.icu.util.CurrencyAmount
import android.os.Build
import android.util.Log
import android.widget.Space
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.example.expensetracker.viewmodel.IncomeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Transaction
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.Income
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.exp

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen( expenseViewModel: ExpenseViewModel = viewModel(), incomeViewModel: IncomeViewModel = viewModel()){

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally) {

        var currentMonth by remember {
            mutableStateOf(LocalDate.now())
        }

        var showAddTransactionDialog by remember {
            mutableStateOf(false)
        }

        var editTransaction by remember { //this is to store the transaction to be edited
            mutableStateOf<Any?>(null)
        }
        
        monthSelector(
            currentMonth = currentMonth,
            onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
            onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
        )

        // Observe the incomes and expenses LiveData - this 2 lines resolved the error of showing "no expenses or incomes.." when the app is launched
        //observeAsState is an extension function in Compose that allows u to observe LiveData in a reactive way , meaning
        val incomesState = incomeViewModel.allIncomes.observeAsState()
        val expensesState = expenseViewModel.allExpense.observeAsState()

        //get the list of incomes and expense
        val incomes = incomesState.value?.filterByMonth(currentMonth)?:emptyList()
        val expenses = expensesState.value?.filterByMonth(currentMonth)?:emptyList()

        //Log the data to check if income and expense list is correctly being fetched
        Log.d("Main Screen","Incomes : $incomes")
        Log.d("Main Screen", "Expenses : $expenses")

        //calculate total income and total expense
        val totalIncome = calculateTotalIncome(incomes)
        val totalExpense = calculateTotalExpense(expenses)

        //calculate total balance
        val totalBalance = calculateTotalBalance(totalIncome,totalExpense)

        /*TotalBalanceDisplay(totalBalance)

        Spacer(modifier = Modifier.height(8.dp))

        TotalIncomeExpenseDisplay(totalIncome,totalExpense)*/

        SummaryCardSection(totalBalance = totalBalance, totalIncome = totalIncome, totalExpense = totalExpense)

        Spacer(modifier = Modifier.height(16.dp))

        //displays the combined list of expenses and incomes
        //combine income and expense into one list
        //val combinedList = combineIncomeExpense(incomes,expenses)

        CombinedListDisplay(
            incomes,
            expenses,
            //TODO - edit transaction
            onEditTransaction = {transaction ->
                editTransaction = transaction //set the transaction to be edited
                showAddTransactionDialog = true //Show the dialog
            },
            onDeleteTransaction = {transaction ->
                //delete the transaction from the viewmodel
                when(transaction){
                    is Income -> incomeViewModel.delete(transaction)
                    is Expense -> expenseViewModel.delete(transaction)
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        AddExpenseIncomeButton(onClick = {showAddTransactionDialog = true})

        if(showAddTransactionDialog){//show the add txn dialogue if the button is clicked
            val transactionToEdit = editTransaction
            AddTransactionDialogue(
                onDismiss = {showAddTransactionDialog = false},
                onAddTransaction = {transactionType , category , amount ->
                    if(transactionToEdit == null){
                        //adding a new transaction
                        if(transactionType == "Income"){
                            incomeViewModel.insert(Income(title = "Income", amount = amount, date = System.currentTimeMillis() , category = category))
                        }else if (transactionType == "Expense"){
                            expenseViewModel.insert(Expense(title = "Expense", amount = amount,date = System.currentTimeMillis() , category = category))
                        }
                    }else{
                        when(transactionToEdit){
                            is Income -> {
                                if(transactionType == "Expense"){
                                    incomeViewModel.delete(transactionToEdit)
                                    expenseViewModel.insert(
                                        Expense(title = transactionType, amount = amount, date = System.currentTimeMillis(), category = category)
                                    )
                                }else{
                                    incomeViewModel.update(
                                        transactionToEdit.copy(
                                            title = transactionType,
                                            category = category,
                                            amount = amount
                                        )
                                    )
                                }
                            }
                            is Expense -> {
                                if(transactionType == "Income"){
                                    expenseViewModel.delete(transactionToEdit)
                                    incomeViewModel.insert(
                                        Income(title = transactionType, amount = amount, date = System.currentTimeMillis(), category = category)
                                    )
                                }else{
                                    expenseViewModel.update(
                                        transactionToEdit.copy(
                                            title = transactionType,
                                            category = category,
                                            amount = amount
                                        )
                                    )
                                }
                            }
                        }

                    }
                    showAddTransactionDialog = false //hide the dialogue after adding the txn
                    editTransaction = null
                },
                transaction = transactionToEdit
            )
        }
    }
}

fun calculateTotalIncome(incomes:List<Income>): Double {
    return incomes.sumOf { it.amount }
}

fun calculateTotalExpense(expenses:List<Expense>): Double{
    return expenses.sumOf { it.amount }
}

fun calculateTotalBalance(totalIncome: Double, totalExpense: Double):Double{
    return totalIncome - totalExpense
}

@Composable
fun TotalBalanceDisplay(totalBalance: Double){
    Text(
        text = "Total Balance : ${totalBalance}",
        style = MaterialTheme.typography.displayMedium.copy(
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun TotalIncomeExpenseDisplay(totalIncome: Double,totalExpense: Double){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Text(
            text = "Total Income : ${totalIncome}",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Total Expense : ${totalExpense}",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            color = MaterialTheme.colorScheme.onBackground
        )
    }

}

@Composable
fun CombinedListDisplay(
    incomes: List<Income>,
    expenses: List<Expense>,
    onEditTransaction: (Any) -> Unit,
    onDeleteTransaction: (Any) -> Unit
    ){
    if(incomes.isEmpty() && expenses.isEmpty()){
        Text(
            text = "No expenses or incomes. Press the add button to add them to the list.",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )
    }else{
        //merging both list and soring on the basis of date
        val combinedSortedList = (incomes + expenses).sortedByDescending { item ->
            when(item){
                is Income -> item.date//access date property of income
                is Expense -> item.date//access date property of expense
                else -> 0L //default value for invalid case
            }
        }
        RecentTransactions(combinedSortedList, onEditTransaction, onDeleteTransaction)
    }
}

//Add button
@Composable
fun AddExpenseIncomeButton(onClick: () -> Unit) {
    Button(
        onClick = onClick, // When button is clicked, it runs the passed function
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "Add")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialogue(
    onDismiss: () -> Unit,
    onAddTransaction: (String, String, Double) -> Unit,
    transaction: Any? = null//Optional parameter to pass the transaction to edit
) {
    var transactionType by remember {
        mutableStateOf("Income")
    }
    var category by remember {
        mutableStateOf("")
    }
    var amount by remember {
        mutableStateOf("")
    }

    var expanded by remember {
        mutableStateOf(false)
    }

    //category list for incomes and expenses
    val incomeCategories = listOf("Salary", "Side-income", "Business", "Others")
    val expenseCategories = listOf("Groceries", "Rent", "Utilities", "Transportation", "Entertainment", "Investment", "Others")

    var selectedCategory by remember {
        mutableStateOf("")
    }
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            if(transaction == null)
                Text(text = "Add transaction")
            else
                Text(text = "Edit transaction")
        },
        text = {
            Column {
                // Transaction type (income or expense)
                Row {
                    TextButton(onClick = { transactionType = "Income" }, colors = ButtonDefaults.textButtonColors()) {
                        Text(text = "Income")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { transactionType = "Expense" }, colors = ButtonDefaults.textButtonColors()) {
                        Text(text = "Expense")
                    }
                }

                var categories = if (transactionType == "Income") incomeCategories else expenseCategories

                DropDownMenu(items = categories, selectedItem = {category = it}, defaultItem = "Select Category")

                Spacer(modifier = Modifier.height(16.dp))

                // Amount input
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(text = "Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                if (parsedAmount > 0 && category.isNotEmpty()) {
                    onAddTransaction(transactionType, category, parsedAmount)
                    onDismiss()//dismiss the dialogue raef doubt
                    Log.d("AddTransaction", "Transaction added: Type=$transactionType, Category=$category, Amount=$parsedAmount")
                } else {
                    Log.d("AddTransaction", "Invalid input: Category=$category, Amount=$amount")
                }
            }) {
                if(transaction == null){
                    Text(text = "Add")
                }else{
                    Text(text = "Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = "Cancel")
            }
        }
    )
}

@Composable
fun DropDownMenu(
   items: List<String>,
   selectedItem: (String) -> Unit,
   modifier: Modifier = Modifier,
   defaultItem: String = items.firstOrNull() ?: "",
   label: String = "Select an item"
){
    val isDropDownExpanded = remember {
        mutableStateOf(false)
    }

    val selectedItemText = remember {
        mutableStateOf(defaultItem)
    }

    Box(modifier = modifier){
        Row (
            modifier = Modifier
                .clickable { isDropDownExpanded.value = true }
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(text = selectedItemText.value)
            //here we can include an icon(optional)
        }

        DropdownMenu(expanded = isDropDownExpanded.value, onDismissRequest = {isDropDownExpanded.value = false }) {
            items.forEach{ item -> 
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = {
                        selectedItemText.value = item
                        selectedItem(item)// Call the callback with the selected item
                        isDropDownExpanded.value = false
                    }
                )
            }
        }
    }
}
//TransactionRow - this function is for clean display of each transaction
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionRow(label: String,category: String,amount: Double,color: Color,onEdit: ()->Unit,onDelete: ()->Unit){

    var showMenu by remember {
        mutableStateOf(false)
    }

    Text(
        text = "$label : $category - $amount",
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
        color = color,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(onClick = {}, onLongClick = { showMenu = true })//to activate edit or delete menu , we have to long press the txn
    )
    
    if(showMenu){
        AlertDialog(
            onDismissRequest = { showMenu = false },
            title = { Text(text = "Choose Action") },
            text = {
                Column {
                    Button(onClick = {
                        onEdit()
                        showMenu = false
                    }) {
                        Text(text = "Edit")
                    }
                    Button(onClick = { 
                        onDelete()
                        showMenu = false
                    }) {
                        Text(text = "Delete")
                    }
                }
            },
            confirmButton = { /*TODO*/ })
    }
    
}

//SummaryCard - This function is to display cards for total balance ,income and expense
@Composable
fun SummaryCard(title: String , amount: Double , backgroundColor: Color , modifier: Modifier = Modifier){
    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$amount",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SummaryCardSection(totalBalance: Double , totalIncome: Double , totalExpense: Double){
    Column {
        SummaryCard(
            title = "Total Balance",
            amount = totalBalance,
            backgroundColor = Color(0xFF1A237E),//dark blue
            modifier = Modifier.fillMaxWidth()
        )
    }
    //one row for income box and expense box
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly //to spread evenly
    ){
        SummaryCard(
            title = "Total Income",
            amount = totalIncome,
            backgroundColor = Color(0xFF8BC34A), // Light Green
            modifier = Modifier.weight(1f)// Occupy half of the width)
        )
        SummaryCard(
            title = "Total Expense",
            amount = totalExpense,
            backgroundColor = Color(0xFFFFCDD2),// Light Red
            modifier = Modifier.weight(1f)//Occupy half of the width
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionCard(
    label: String,
    category: String,
    amount: Double,
    isExpense: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
){

    var showMenu by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = { showMenu = true }),//to activate edit or delete menu , we have to long press the txn
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp,Color.LightGray),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {

        if(showMenu){
            AlertDialog(
                onDismissRequest = { showMenu = false },
                title = { Text(text = "Choose Action") },
                text = {
                    Column {
                        Button(onClick = {
                            onEdit()
                            showMenu = false
                        }) {
                            Text(text = "Edit")
                        }
                        Button(onClick = {
                            onDelete()
                            showMenu = false
                        }) {
                            Text(text = "Delete")
                        }
                    }
                },
                confirmButton = { /*TODO*/ })
        }

        Row (
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            Column {
                //description to be added to the database
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Text(
                text = "$amount",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isExpense) Color.Red else Color.Green,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RecentTransactions(
    transactions: List<Any>,
    onEditTransaction: (Any) -> Unit,//pass the whole transaction object for editing
    onDeleteTransaction: (Any) -> Unit//pass the whole transaction object for deleting
    ){
    Text(
        text = "Recent Transactions",
        modifier = Modifier.padding(8.dp),
        style = MaterialTheme.typography.headlineSmall
    )
    LazyColumn (
        modifier = Modifier
            .fillMaxHeight(0.6f)
            .padding(vertical = 8.dp)
    ){
        items(transactions) { transaction ->
            val isExpense = when(transaction){
                is Income -> false
                is Expense -> true
                else -> return@items// Skip if the transaction is neither Income nor Expense
            }
            val title = if (transaction is Income) transaction.title else (transaction as Expense).title
            val category = if (transaction is Income) transaction.category else (transaction as Expense).category
            val amount = if (transaction is Income) transaction.amount else (transaction as Expense).amount

            TransactionCard(
                label = title,
                category = category,
                amount = amount,
                isExpense = isExpense,
                onEdit = {onEditTransaction(transaction)},
                onDelete = {onDeleteTransaction(transaction)}
            )
        }
    }
}

//Function to filter transactions on the basis of selected month    //doubt-need to study
//<T> means generic , it means it can work with any type of data like expense or income
//this - refers to the list on which the function is called
//filter - iterates over each item of the list and executes the code inside the braces
@RequiresApi(Build.VERSION_CODES.O)
fun <T> List<T>.filterByMonth(selectedMonth: LocalDate): List<T> {
    return this.filter {
        val transactionDate = when (it){
            is Income -> it.date
            is Expense -> it.date
            else -> return@filter false
        }
        val localDate = Instant.ofEpochMilli(transactionDate).atZone(ZoneId.systemDefault()).toLocalDate()  //converting the date to localdate
        localDate.month == selectedMonth.month && localDate.year == selectedMonth.year  //checking if the transaction matches the selected month and year
    }
}


//month selector composable
@RequiresApi(Build.VERSION_CODES.O) //I added this because Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Previous Month") was not supported on api sdk lower than 26
@Composable
fun monthSelector(
    currentMonth: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
){
    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ){
        IconButton(onClick = onPreviousMonth) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
        }
        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        IconButton(onClick = onNextMonth) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
        }
    }
}