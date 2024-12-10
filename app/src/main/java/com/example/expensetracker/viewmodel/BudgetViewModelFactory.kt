package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expensetracker.repository.BudgetRepository
import com.example.expensetracker.repository.ExpenseRepository

class BudgetViewModelFactory(
    private val repository: BudgetRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(repository,expenseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
