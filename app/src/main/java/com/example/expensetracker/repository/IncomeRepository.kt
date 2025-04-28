package com.example.expensetracker.repository

import com.example.expensetracker.dao.IncomeDao
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.Income
import kotlinx.coroutines.flow.Flow

class IncomeRepository(private val incomeDao: IncomeDao) {

    fun getAllIncomes(): Flow<List<Income>> {
        val incomes =  incomeDao.getAllIncomes()
        android.util.Log.d("IncomeRepository", "Fetched Incomes : $incomes")
        return incomes
    }

    suspend fun insert(income: Income){
        android.util.Log.d("IncomeRepository","Inserting income : $income")
        incomeDao.insertIncome(income)
    }

    suspend fun delete(income: Income){
        android.util.Log.d("IncomeRepository","Deleting income : $income")
        incomeDao.deleteIncome(income)
    }

    suspend fun update(income: Income){
        android.util.Log.d("IncomeRepository","Updating income : $income")
        incomeDao.updateIncome(income)
    }

    suspend fun getTotalIncomeByCategory(category: String,month: String): Double? {
        val totalIncomeByCategory = incomeDao.getTotalIncomeByCategory(category,month)
        android.util.Log.d("IncomeRepository","Total Income of $category : $totalIncomeByCategory")
        return totalIncomeByCategory
    }

    fun getIncomesByCategory(category: String): Flow<List<Income>> {
        return incomeDao.getIncomesByCategory(category)
    }

}