package com.example.expensetracker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.model.Expense
import com.example.expensetracker.repository.ExpenseRepository
import kotlinx.coroutines.launch

class ExpenseViewModel(private val repository: ExpenseRepository):ViewModel(){
    private val _allExpenses = MutableLiveData<List<Expense>>() //this is mutable live data list of expenses , which can be modified within viewModel
    val allExpense : LiveData<List<Expense>> = _allExpenses//this is Live data list of expenses which is read only data and cannot be modified

    init {
        fetchAllExpenses()//fetches all the expenses when the viewmodel is initialized , so that viemodel starts with latest data , so UI can display that data
    }

    private fun fetchAllExpenses(){
        viewModelScope.launch {
            _allExpenses.value = repository.getAllExpenses()
        }
    }

    fun insert(expense: Expense){
        viewModelScope.launch {
            repository.insert(expense)
            fetchAllExpenses()//update the list
        }
    }

    fun delete(expense: Expense){
        viewModelScope.launch {
            repository.delete(expense)
            fetchAllExpenses()//update the list
        }
    }
}