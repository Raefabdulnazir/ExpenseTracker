package com.moneynest.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.moneynest.expensetracker.repository.IncomeRepository

class IncomeViewModelFactory(private val repository: IncomeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(IncomeViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return IncomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/*
* A viewModel is like a manager for our UI data. It holds the data that we want to show in our app's screen
* Android creates viewModel automatically without any arguments. But if the viewmodel needs extra information to work(like a repository) , we need a factory to create it and pass that information in.
* In our case , IncomeViewModel requires incomeRepository to access the income data from the database . You can't pass this directly when Android creates viewModel on its own, so the Factory helps to pass the dependency(the repository) into the viewmodel
*/
