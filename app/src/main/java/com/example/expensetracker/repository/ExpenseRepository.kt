package com.example.expensetracker.repository

import com.example.expensetracker.dao.ExpenseDao
import com.example.expensetracker.model.Expense

class ExpenseRepository(private val expenseDao: ExpenseDao){

    suspend fun getAllExpenses(): List<Expense> {
        val expenses =  expenseDao.getAllExpenses()
        android.util.Log.d("ExpenseRepository","All expenses : $expenses")
        return expenses
    }

    suspend fun insert(expense: Expense){
        android.util.Log.d("ExpenseRepository","Inserting expense : $expense")
        expenseDao.insertExpense(expense)
    }

    suspend fun delete(expense: Expense){
        android.util.Log.d("ExpenseRepository","Deleting expense : $expense")
        expenseDao.deleteExpense(expense)
    }
}