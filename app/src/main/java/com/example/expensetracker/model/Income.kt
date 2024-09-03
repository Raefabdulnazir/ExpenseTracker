package com.example.expensetracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("income_table")
data class Income (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val date: Long,// storing the date as timestamp
    val category: String
)