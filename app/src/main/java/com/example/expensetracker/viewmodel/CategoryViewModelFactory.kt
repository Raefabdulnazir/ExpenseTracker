package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expensetracker.repository.BudgetRepository
import com.example.expensetracker.repository.CategoryRepository
import com.example.expensetracker.repository.ExpenseRepository
import com.example.expensetracker.repository.IncomeRepository

class CategoryViewModelFactory(private val repository: CategoryRepository,
                               private val expenseRepository: ExpenseRepository,
                               private val incomeRepository: IncomeRepository,
                                private val budgetRepository: BudgetRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(repository,expenseRepository,incomeRepository,budgetRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}