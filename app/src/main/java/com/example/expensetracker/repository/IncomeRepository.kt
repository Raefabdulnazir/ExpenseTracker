package com.example.expensetracker.repository

import com.example.expensetracker.dao.IncomeDao
import com.example.expensetracker.model.Income

class IncomeRepository(private val incomeDao: IncomeDao) {

    suspend fun getAllIncomes(): List<Income>{
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

}