package com.example.expensetracker.dao

//Data Access Object defines the methods that we want to interact with our database
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import com.example.expensetracker.model.Expense

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expense_table ORDER BY date DESC")
    suspend fun getAllExpenses(): List<Expense>

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)
}