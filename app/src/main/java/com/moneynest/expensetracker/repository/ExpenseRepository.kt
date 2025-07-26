package com.moneynest.expensetracker.repository

import com.moneynest.expensetracker.dao.ExpenseDao
import com.moneynest.expensetracker.model.Expense
import com.moneynest.expensetracker.viewmodel.BudgetViewModel
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    //private val budgetViewModel: BudgetViewModel
){
    //declare a lateinit variable(late initialised) for budgetViewModel
    //This is done to avoid circular dependency during initialization
    /*DETAILED INFO(why did we do this)
    *ExpenseRepository and BudgetViewModel are interdependent . that is they both need each other during initialisation
    * */
    //lateinit var budgetViewModel: BudgetViewModel

    private var _budgetViewModel: BudgetViewModel? = null

    var budgetViewModel: BudgetViewModel?
        get() = _budgetViewModel
        set(value) {
            _budgetViewModel = value
        }

    fun getAllExpenses(): Flow<List<Expense>> {
        val expenses =  expenseDao.getAllExpenses()
        android.util.Log.d("ExpenseRepository","All expenses : $expenses")
        return expenses
    }

    suspend fun insert(expense: Expense){
        android.util.Log.d("ExpenseRepository","Inserting expense : $expense")
        expenseDao.insertExpense(expense)
        // `::budgetViewModel.isInitialized` is a Kotlin reflection check to see if the lateinit variable has a value
/*        if(::budgetViewModel.isInitialized){
            budgetViewModel.updateSpentForCategory(expense.category)
        }*/
        budgetViewModel?.updateSpentForCategory(expense.category,expense.month) // Conditional update
    }

    suspend fun delete(expense: Expense){
        android.util.Log.d("ExpenseRepository","Deleting expense : $expense")
        expenseDao.deleteExpense(expense)
        /*if(::budgetViewModel.isInitialized){
            budgetViewModel.updateSpentForCategory(expense.category)
        }*/
        budgetViewModel?.updateSpentForCategory(expense.category,expense.month) // Conditional update
    }

    suspend fun update(expense: Expense){
        android.util.Log.d("ExpenseRepository","Updating expense : $expense")
        expenseDao.updateExpense(expense)
        /*if(::budgetViewModel.isInitialized){
            budgetViewModel.updateSpentForCategory(expense.category)
        }*/
        budgetViewModel?.updateSpentForCategory(expense.category,expense.month) // Conditional update
    }

    suspend fun getTotalSpentByCategory(category: String,month: String): Double? {
        val totalexpenseByCategory = expenseDao.getTotalSpentByCategory(category,month)
        android.util.Log.d("ExpenseRepository","Total Expense of $category : $totalexpenseByCategory")
        return totalexpenseByCategory
    }

    fun getExpensesByCategory(category: String): Flow<List<Expense>> {
        return expenseDao.getExpensesByCategory(category)
    }

}