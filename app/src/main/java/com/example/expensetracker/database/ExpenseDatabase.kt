package com.example.expensetracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.expensetracker.dao.ExpenseDao
import com.example.expensetracker.dao.IncomeDao
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.Income

@Database(entities = [Expense::class, Income::class], version = 1)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao

    companion object {
        //@Volatile enures that INSTANCE variable is visible to all the threads
        //It helps in preventing issues where multiple threads access the INSTANCE at the same time.
        @Volatile
        private var INSTANCE: ExpenseDatabase? = null
        //getDatabase is a function that returns single instance of database
        fun getDatabase(context: Context): ExpenseDatabase {
            //if INSTANCE is null , code inside synchronized block will be executed
            return INSTANCE ?: synchronized(this) {
                //synchronized ensures that only one thread can execute this block of code at a time
                val instance = Room.databaseBuilder(
                    context.applicationContext,//use applicationContext to avoid memory leaks
                    ExpenseDatabase::class.java,
                    "expense_database"//name of the database file
                ).build()
                //set the INSTANCE variable to the newly created database instance
                INSTANCE = instance
                instance
            }
        }
    }
}
