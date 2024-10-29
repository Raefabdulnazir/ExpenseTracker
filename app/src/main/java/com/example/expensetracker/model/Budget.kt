package com.example.expensetracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_table")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val month: String,
    //val totalBudget: Double,
    val categoryName: String,
    val categoryBudget: Double,
    val categorySpent: Double = 0.0,
    val alertThreshold: Double = 0.0//threshold to trigger alerts
)