package com.example.expensetracker.screens

import android.widget.Space
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.example.expensetracker.viewmodel.IncomeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.Income

@Composable
fun MainScreen( expenseViewModel: ExpenseViewModel = viewModel(), incomeViewModel: IncomeViewModel = viewModel()){

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        //get the list of incomes and expense
        val incomes = incomeViewModel.allIncomes.value?:emptyList()
        val expenses = expenseViewModel.allExpense.value?:emptyList()

        //combine income and expense into one list
        val combinedList = combineIncomeExpense(incomes,expenses)

        //calculate total income and total expense
        val totalIncome = calculateTotalIncome(incomes)
        val totalExpense = calculateTotalExpense(expenses)

        //calculate total balance
        val totalBalance = calculateTotalBalance(totalIncome,totalExpense)

        TotalBalanceDisplay(totalBalance)
        
        Spacer(modifier = Modifier.height(16.dp))

        TotalIncomeExpenseDisplay(totalIncome,totalExpense)

        Spacer(modifier = Modifier.height(16.dp))

        //displays the combined list of expenses and incomes
        CombinedListDisplay(combinedList)
        
        Spacer(modifier = Modifier.height(16.dp))

/*        AddExpenseIncomeButton{
            //a new pop up screen , where user can enter whether its
            //income or expense , its description and amount
        }*/
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
        style = MaterialTheme.typography.displayMedium,
        color = MaterialTheme.colorScheme.primary
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
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Total Expense : ${totalExpense}",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }

}

fun combineIncomeExpense(incomes: List<Income>,expenses: List<Expense>):List<Any>{
    return incomes + expenses
}

@Composable
fun CombinedListDisplay(items: List<Any>){
    LazyColumn {
        items(items){ item ->
            when(item){
                is Income ->{
                    Text(
                        text = "Income : ${item.category} - ${item.amount}",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                is Expense ->{
                    Text(
                        text = "Expense : ${item.category} - ${item.amount}",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}