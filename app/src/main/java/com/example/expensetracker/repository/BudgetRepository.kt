package com.example.expensetracker.repository

import com.example.expensetracker.dao.BudgetDao
import com.example.expensetracker.model.Budget

class BudgetRepository(private val budgetDao: BudgetDao) {

    suspend fun getAllBudgets(): List<Budget> {
        val budgets = budgetDao.getAllBudget()
        android.util.Log.d("BudgetRepository","All budgets : $budgets")
        return budgets
    }

    suspend fun getBudgetByMonth(month: String): List<Budget>{
        val budgets = budgetDao.getBudgetByMonth(month)
        android.util.Log.d("BudgetRepository","All budgets of $month : $budgets")
        return budgets
    }

    suspend fun insert(budget:Budget) {
        android.util.Log.d("BudgetRepository","Inserting budget : $budget")
        budgetDao.insertBudget(budget)
    }

    suspend fun delete(budget: Budget) {
        android.util.Log.d("BudgetRepository","Deleting budget : $budget")
        budgetDao.deleteBudget(budget)
    }

    suspend fun update(budget: Budget) {
        android.util.Log.d("BudgetRepository","Updating budget : $budget")
        budgetDao.updateBudget(budget)
    }

    suspend fun getBudgetByCategory(categoryName: String): Budget? {
       return budgetDao.getBudgetByCategory(categoryName)
    }
}