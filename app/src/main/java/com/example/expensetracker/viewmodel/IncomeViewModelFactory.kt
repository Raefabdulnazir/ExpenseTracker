package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expensetracker.repository.IncomeRepository

class IncomeViewModelFactory(private val repository: IncomeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(IncomeViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return IncomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}