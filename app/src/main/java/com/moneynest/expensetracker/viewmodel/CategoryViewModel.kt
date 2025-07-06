package com.moneynest.expensetracker.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneynest.expensetracker.model.Category
import com.moneynest.expensetracker.repository.BudgetRepository
import com.moneynest.expensetracker.repository.CategoryRepository
import com.moneynest.expensetracker.repository.ExpenseRepository
import com.moneynest.expensetracker.repository.IncomeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(private val categoryRepository: CategoryRepository,
                        private val expenseRepository: ExpenseRepository,
                        private val incomeRepository: IncomeRepository,
                        private val budgetRepository: BudgetRepository
) : ViewModel() {

/*    private val _incomeCategories = MutableStateFlow<List<Category>>(emptyList())
    val incomeCategories : StateFlow<List<Category>> = _incomeCategories

    private val _expenseCategories = MutableStateFlow<List<Category>>(emptyList())
    val expenseCategories : StateFlow<List<Category>> = _expenseCategories*/

    val incomeCategories: StateFlow<List<Category>> =
        categoryRepository.getAllCategories("Income")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseCategories: StateFlow<List<Category>> =
        categoryRepository.getAllCategories("Expense")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

/*    init {
        loadIncomeCategories()
        loadExpenseCategories()
    }

    private fun loadIncomeCategories(){
        viewModelScope.launch {
            try {
                val categories = categoryRepository.getAllCategories("Income")
                _incomeCategories.value = categories
                Log.d("CategoryViewModel","Fetched Income Categories : $_incomeCategories")
            }
            catch(e:Exception) {
                Log.e("CategoryViewModel","Error fetching income categories",e)
            }
        }
    }*/

/*    private fun loadExpenseCategories(){
        viewModelScope.launch {
            try{
                val categories = categoryRepository.getAllCategories("Expense")
                _expenseCategories.value = categories
                Log.d("CategoryViewModel","Fetched Expense Categories : $_expenseCategories")
            }
            catch (e:Exception){
                Log.e("CategoryViewModel","Error fetching expense categories",e)
            }
        }
    }*/

    fun addCategory(name : String , type : String){
        val newCategory = Category(name = name , type = type)
        viewModelScope.launch {
            try{
                categoryRepository.insertCategory(newCategory)
                Log.d("CategoryViewModel", "Inserting Category : $newCategory")

/*                if(type == "Income"){
                    _incomeCategories.update { it + newCategory }
                }else{
                    _expenseCategories.update { it + newCategory }
                }*/
            }
            catch (e:Exception){
                Log.e("CategoryViewModel","Error adding category",e)
            }

        }
    }

    fun deleteCategory(category: Category){
        viewModelScope.launch {
            try{
                categoryRepository.deleteCategory(category)
                Log.d("CategoryViewModel", "Deleting Category : $category")

                if (category.type == "Income") {
                    incomeRepository.getIncomesByCategory(category.name).collect { incomeList ->
                        incomeList.forEach {
                            incomeRepository.delete(it)
                        }
                    }
                } else {
                    expenseRepository.getExpensesByCategory(category.name).collect { expenseList ->
                        expenseList.forEach {
                            expenseRepository.delete(it)
                        }

                        val budget = budgetRepository.getBudgetByCategory(category.name)
                        if(budget != null){
                            budgetRepository.delete(budget)
                            Log.d("CategoryViewModel", "Deleted budget for category: ${category.name}")
                        }

                    }
                }
            }
            catch (e:Exception){
                Log.e("CategoryViewModel","Error deleting category",e)
            }

        }
    }

}