package com.example.expensetracker.repository

import com.example.expensetracker.dao.IncomeDao
import com.example.expensetracker.model.Income

class IncomeRepository(private val incomeDao: IncomeDao) {

    suspend fun getAllIncomes(): List<Income>{
        return incomeDao.getAllIncomes()
    }

    suspend fun insert(income: Income){
        incomeDao.insertIncome(income)
    }

    suspend fun delete(income: Income){
        incomeDao.deleteIncome(income)
    }

}