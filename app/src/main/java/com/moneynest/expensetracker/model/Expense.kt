package com.moneynest.expensetracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("expense_table")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val date: Long,//storing the data as timestamp
    val category: String,
    val month: String//store in "YYYY-MM" format //this was updated to update the budgetspent in each budget category
)