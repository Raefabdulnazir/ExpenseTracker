package com.example.expensetracker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.Income
import com.example.expensetracker.repository.IncomeRepository
import kotlinx.coroutines.launch

class IncomeViewModel(private val repository: IncomeRepository):ViewModel() {
    private val _allIncomes = MutableLiveData<List<Income>>()
    val allIncomes : LiveData<List<Income>> = _allIncomes

    init{
        fetchAllIncomes()
    }

    private fun fetchAllIncomes(){
        viewModelScope.launch{
            _allIncomes.value = repository.getAllIncomes()
        }
    }

    fun insert(income: Income){
        viewModelScope.launch {
            repository.insert(income)
            fetchAllIncomes()//updating the list
        }
    }

    fun delete(income: Income){
        viewModelScope.launch {
            repository.delete(income)
            fetchAllIncomes()//updating the list
        }
    }
}