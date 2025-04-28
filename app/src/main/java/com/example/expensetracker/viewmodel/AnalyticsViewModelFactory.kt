package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expensetracker.repository.CategoryRepository
import com.example.expensetracker.repository.ExpenseRepository
import com.example.expensetracker.repository.IncomeRepository

class AnalyticsViewModelFactory(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnalyticsViewModel(expenseRepository,incomeRepository,categoryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}