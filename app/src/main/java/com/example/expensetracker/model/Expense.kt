package com.example.expensetracker.model

import android.icu.text.CaseMap.Title
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date
import java.time.temporal.TemporalAmount

@Entity("expense_table")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val date: Long,//storing the data as timestamp
    val category: String
)