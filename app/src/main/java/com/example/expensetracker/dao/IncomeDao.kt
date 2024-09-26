package com.example.expensetracker.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete

import androidx.room.*
import com.example.expensetracker.model.Income

@Dao
interface IncomeDao {
    @Insert
    suspend fun insertIncome(income: Income)

    @Query("SELECT * FROM income_table ORDER BY date DESC")
    suspend fun getAllIncomes(): List<Income>

    @Delete
    suspend fun deleteIncome(income: Income)

    @Update
    suspend fun updateIncome(income: Income)
}