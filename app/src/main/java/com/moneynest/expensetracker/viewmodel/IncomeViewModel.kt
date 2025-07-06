package com.moneynest.expensetracker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.moneynest.expensetracker.model.Income
import com.moneynest.expensetracker.repository.IncomeRepository
import kotlinx.coroutines.launch

class IncomeViewModel(private val repository: IncomeRepository):ViewModel() {
/*    private val _allIncomes = MutableLiveData<List<Income>>()
    val allIncomes : LiveData<List<Income>> = _allIncomes*/

    val allIncomes: LiveData<List<Income>> = repository.getAllIncomes().asLiveData()

/*    init{
        fetchAllIncomes()
    }*/

/*    *//*private*//* fun fetchAllIncomes(){
        viewModelScope.launch{
            val incomes = repository.getAllIncomes()
            _allIncomes.value = incomes
            android.util.Log.d("IncomeViewModel","All incomes : $incomes")
        }
    }*/

    fun insert(income: Income){
        viewModelScope.launch {
            android.util.Log.d("IncomeViewModel","Inserting income : $income")
            repository.insert(income)
            //fetchAllIncomes()//updating the list
        }
    }

    fun delete(income: Income){
        viewModelScope.launch {
            android.util.Log.d("IncomeViewModel","Deleting income : $income")
            repository.delete(income)
            //fetchAllIncomes()//updating the list
        }
    }

    fun update(income: Income){
        viewModelScope.launch {
            android.util.Log.d("IncomeViewModel","Updating income : $income")
            repository.update(income)
            //fetchAllIncomes()
        }
    }
}