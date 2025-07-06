package com.moneynest.expensetracker.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.*
import com.moneynest.expensetracker.model.Income
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Insert
    suspend fun insertIncome(income: Income)

    @Query("SELECT * FROM income_table ORDER BY date DESC")
    fun getAllIncomes(): Flow<List<Income>>

    @Query("SELECT * FROM income_table WHERE category = :category")
    fun getIncomesByCategory(category: String): Flow<List<Income>>

    @Delete
    suspend fun deleteIncome(income: Income)

    @Update
    suspend fun updateIncome(income: Income)

    @Query("SELECT SUM(amount) FROM income_table WHERE category = :category AND month = :month")
    suspend fun getTotalIncomeByCategory(category: String,month: String): Double?

}