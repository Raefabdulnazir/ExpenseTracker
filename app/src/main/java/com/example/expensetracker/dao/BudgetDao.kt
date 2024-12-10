package com.example.expensetracker.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expensetracker.model.Budget

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Update
    suspend fun updateBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Query("SELECT * FROM budget_table")
    suspend fun getAllBudget():List<Budget>

    @Query("SELECT * FROM budget_table WHERE month = :month")
    suspend fun getBudgetByMonth(month: String):List<Budget>

    @Query("SELECT * FROM budget_table WHERE categoryName = :categoryName LIMIT 1")//LIMIT 1 is used to specify that only first result should be returned , even if there are multiple records matching the criteria.
    suspend fun getBudgetByCategory(categoryName: String): Budget?//Budget? means that it could return NULL if no budget exists for the given category name
}