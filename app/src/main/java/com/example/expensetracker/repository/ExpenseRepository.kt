package com.example.expensetracker.repository

import com.example.expensetracker.dao.ExpenseDao
import com.example.expensetracker.model.Expense

class ExpenseRepository(private val expenseDao: ExpenseDao){

    suspend fun getAllExpenses(): List<Expense> {
        return expenseDao.getAllExpenses()
    }

    suspend fun insert(expense: Expense){
        expenseDao.insertExpense(expense)
    }

    suspend fun delete(expense: Expense){
        expenseDao.deleteExpense(expense)
    }
}