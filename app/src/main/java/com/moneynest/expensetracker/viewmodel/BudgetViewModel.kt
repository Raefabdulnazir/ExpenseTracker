package com.moneynest.expensetracker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneynest.expensetracker.model.Budget
import com.moneynest.expensetracker.repository.BudgetRepository
import com.moneynest.expensetracker.repository.ExpenseRepository
import kotlinx.coroutines.launch
import android.util.Log//Import Log class
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn

class BudgetViewModel(
    private val repository: BudgetRepository,
    private val expenseRepository : ExpenseRepository   //I added expenserepository for dynamically updating categorySpent of the category
): ViewModel() {

/*    private val _allBudgets = MutableLiveData<List<Budget>>()
    val allBudgets: LiveData<List<Budget>> = _allBudgets*/

    private val _allBudgets = repository.getAllBudgets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBudgets: StateFlow<List<Budget>> = _allBudgets

    /*init {
        fetchAllBudgets()
    }*/

    /*private fun fetchAllBudgets() {
        viewModelScope.launch {
            val budgets = repository.getAllBudgets()
            _allBudgets.postValue(budgets)
            Log.d("BudgetViewModel", "Fetched All Budgets: $budgets")
        }
    }*/

    fun insert(budget: Budget) {
        viewModelScope.launch {
            android.util.Log.d("BudgetViewModel", "Inserting Budget : $budget")
            repository.insert(budget)
            //fetchAllBudgets()//Refreshing the list after the insertion
        }
    }

    fun delete(budget: Budget) {
        viewModelScope.launch {
            android.util.Log.d("BudgetViewModel", "Deleting Budget : $budget")
            repository.delete(budget)
            //fetchAllBudgets()
        }
    }

    fun update(budget: Budget) {
        viewModelScope.launch {
            android.util.Log.d("BudgetViewModel", "Updating Budget : $budget")
            repository.update(budget)
            //fetchAllBudgets()
        }
    }

    fun getBudgetByMonth(month: String): LiveData<List<Budget>> {
        return repository.getBudgetByMonth(month).asLiveData()
    }

    fun saveOrUpdateBudget(budget: Budget) {
        viewModelScope.launch {
            Log.d("BudgetViewModel", "Saving or Updating Budget: $budget")
            val existingBudgets = repository.getBudgetByMonth(budget.month).first()
            val existingBudget = existingBudgets.find { it.categoryName == budget.categoryName }

            if (existingBudget != null) {
                repository.update(budget)
            } else {
                repository.insert(budget)
            }
        }
    }

    // Call this function after each transaction add, update, or delete
    fun updateSpentForCategory(categoryName: String, month: String) {
        viewModelScope.launch {
            val totalSpent = expenseRepository.getTotalSpentByCategory(categoryName, month) ?: 0.0
            val budget = repository.getBudgetByCategory(categoryName)
            if (budget != null) {
                budget.categorySpent = totalSpent
                repository.update(budget)
            }
        }
    }

     suspend fun getTotalSpentForCategory(categoryName: String,month: String): Double?{   //newly added - to get the totals of each category
        return expenseRepository.getTotalSpentByCategory(categoryName,month)
    }

    fun fetchInitialSpent(categoryName: String,month: String): LiveData<Double> = liveData {
        try{
            val initialSpent = getTotalSpentForCategory(categoryName,month) ?: 0.0
            emit(initialSpent)
        } catch (e: Exception) {
            emit(0.0)
        }
    }

    /*
    * The below function getSpentForCategory wraps the  suspend function(getTotalSpentForCategory)
    * into a livedata for observing changes in reactive manner
    * */
    fun getSpentForCategory(categoryName: String, month: String): LiveData<Double> = liveData {
        val spent = getTotalSpentForCategory(categoryName, month) ?: 0.0
        android.util.Log.d("BudgetViewModel", "Category: $categoryName, Month: $month, Spent: $spent")
        emit(spent)
    }

}
