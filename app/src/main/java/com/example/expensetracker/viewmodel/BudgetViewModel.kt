package com.example.expensetracker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.model.Budget
import com.example.expensetracker.repository.BudgetRepository
import com.example.expensetracker.repository.ExpenseRepository
import kotlinx.coroutines.launch
import java.time.Month
import android.util.Log//Import Log class
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.liveData

class BudgetViewModel(
    private val repository: BudgetRepository,
    private val expenseRepository : ExpenseRepository   //I added expenserepository for dynamically updating categorySpent of the category
): ViewModel() {

    private val _allBudgets = MutableLiveData<List<Budget>>()
    val allBudgets: LiveData<List<Budget>> = _allBudgets

    init {
        fetchAllBudgets()
    }

    private fun fetchAllBudgets() {
        viewModelScope.launch {
            val budgets = repository.getAllBudgets()
            _allBudgets.postValue(budgets)
            Log.d("BudgetViewModel", "Fetched All Budgets: $budgets")
        }
    }

    fun insert(budget: Budget) {
        viewModelScope.launch {
            android.util.Log.d("BudgetViewModel", "Inserting Budget : $budget")
            repository.insert(budget)
            fetchAllBudgets()//Refreshing the list after the insertion
        }
    }

    fun delete(budget: Budget) {
        viewModelScope.launch {
            android.util.Log.d("BudgetViewModel", "Deleting Budget : $budget")
            repository.delete(budget)
            fetchAllBudgets()
        }
    }

    fun update(budget: Budget) {
        viewModelScope.launch {
            android.util.Log.d("BudgetViewModel", "Updating Budget : $budget")
            repository.update(budget)
            fetchAllBudgets()
        }
    }

    fun getBudgetByMonth(month: String): LiveData<List<Budget>> {
        val budgetByMonth = MutableLiveData<List<Budget>>()
        viewModelScope.launch {
            val budgets = repository.getBudgetByMonth(month)
            budgetByMonth.value = budgets//set the result in Livedata
            android.util.Log.d("BudgetViewModel", "All Budgets of month $month : $budgetByMonth")
        }
        return budgetByMonth
    }

    fun saveOrUpdateBudget(budget: Budget) {     //this function is called when creating a new budget alert
        viewModelScope.launch {
            Log.d("BudgetViewModel", "Saving or Updating Budget: $budget")
            val existingBudgets = repository.getBudgetByMonth(budget.month)
            val existingBudget = existingBudgets.find { it.categoryName == budget.categoryName }

            if (existingBudget != null) {
                Log.d("BudgetViewModel", "Existing Budget Found: $existingBudget")
                //budget does exist , so update
                repository.update(budget)
                fetchAllBudgets()
                Log.d("BudgetViewModel", "Updated Budget: $budget")
            } else {
                Log.d("BudgetViewModel", "No Existing Budget Found, Inserting New Budget: $budget")
                //budget does not exist , so inserting
                repository.insert(budget)
                fetchAllBudgets()
                Log.d("BudgetViewModel", "Inserted New Budget: $budget")
            }
            Log.d("BudgetViewModel", "Updated All Budgets LiveData: ${_allBudgets.value}")
        }
    }

    // Call this function after each transaction add, update, or delete
    fun updateSpentForCategory(categoryName: String,month: String) {
        viewModelScope.launch {
            // Fetch the total spent for the category directly
            val totalSpent = expenseRepository.getTotalSpentByCategory(categoryName,month) ?: 0.0
            val budget = repository.getBudgetByCategory(categoryName)

            if (budget != null) {
                // Update the categorySpent field
                budget.categorySpent = totalSpent
                repository.update(budget)

                // Refresh the list of all budgets after update
                fetchAllBudgets()
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
