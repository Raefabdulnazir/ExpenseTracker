package com.example.expensetracker.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expensetracker.dao.BudgetDao
import com.example.expensetracker.dao.CategoryDao
import com.example.expensetracker.dao.ExpenseDao
import com.example.expensetracker.dao.IncomeDao
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.Income
import com.example.expensetracker.model.Budget
import com.example.expensetracker.model.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Database(entities = [Expense::class, Income::class, Budget::class, Category::class], version = 6)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        //@Volatile enures that INSTANCE variable is visible to all the threads
        //It helps in preventing issues where multiple threads access the INSTANCE at the same time.
        @Volatile
        private var INSTANCE: ExpenseDatabase? = null
        //getDatabase is a function that returns single instance of database
        fun getDatabase(context: Context): ExpenseDatabase {

            // Check if INSTANCE is already created
            if (INSTANCE != null) {
                Log.d("ExpenseDatabase", "Returning existing database instance")
            }

            //if INSTANCE is null , code inside synchronized block will be executed
            return INSTANCE ?: synchronized(this) {

                //synchronized ensures that only one thread can execute this block of code at a time
                Log.d("ExpenseDatabase", "Creating new database instance")

                val instance = Room.databaseBuilder(
                    context.applicationContext,//use applicationContext to avoid memory leaks
                    ExpenseDatabase::class.java,
                    "expense_database"//name of the database file
                ).fallbackToDestructiveMigration()
                    .addCallback(PrepopulateCallback(context))
                    .build()//fallbackToDestructiveMigration() destroys and rebuilds the database automatically when scheme change is detected
                //set the INSTANCE variable to the newly created database instance

                // Log after the instance is created
                Log.d("ExpenseDatabase", "Database instance created successfully")

                INSTANCE = instance

                // Log the assignment
                Log.d("ExpenseDatabase", "INSTANCE variable is set")

                instance//return the new db instance
            }
        }

        private class PrepopulateCallback(
            private val context: Context
        ) : RoomDatabase.Callback(){
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d("PrepopulateCallback", "Database onCreate triggered")
                val predefinedIncomeCategories = listOf(
                    Category(name = "Salary", type = "Income"),
                    Category(name = "Side-income", type = "Income"),
                    Category(name = "Business", type = "Income"),
                    Category(name = "Rewards", type = "Income"),
                    Category(name = "Others", type = "Income")
                )

                val predefinedExpenseCategories = listOf(
                    Category(name = "House/Rent", type = "Expense"),
                    Category(name = "Healthcare", type = "Expense"),
                    Category(name = "Shopping", type = "Expense"),
                    Category(name = "Personal Care", type = "Expense"),
                    Category(name = "Education", type = "Expense"),
                    Category(name = "Food", type = "Expense"),
                    Category(name = "Groceries", type = "Expense"),
                    Category(name = "Entertainment", type = "Expense"),
                    Category(name = "Transportation", type = "Expense"),
                    Category(name = "Utilities", type = "Expense"),
                    Category(name = "Other", type = "Expense")
                )

                //Run this task in background
                CoroutineScope(Dispatchers.IO).launch{
                    Log.d("PrepopulateCallback", "Inserting predefined categories")
                    getDatabase(context).categoryDao().apply {
                        predefinedIncomeCategories.forEach { insertCategory(it) }
                        predefinedExpenseCategories.forEach { insertCategory(it) }
                    }
                    Log.d("PrepopulateCallback", "Inserted predefined categories")
                }

            }
        }

    }
}
