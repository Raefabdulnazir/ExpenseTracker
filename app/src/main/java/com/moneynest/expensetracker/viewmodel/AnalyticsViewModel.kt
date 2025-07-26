package com.moneynest.expensetracker.viewmodel

import androidx.compose.ui.graphics.Color
import com.moneynest.expensetracker.model.PieChartData
import com.moneynest.expensetracker.repository.ExpenseRepository
import com.moneynest.expensetracker.ui.theme.categoryColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneynest.expensetracker.repository.CategoryRepository
import com.moneynest.expensetracker.repository.IncomeRepository
import com.moneynest.expensetracker.ui.theme.incomeCategoryColors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val expenseRepository : ExpenseRepository,   //I added expenserepository for getting the category spent to update it on analysis graph
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel(){    //ViewModel superclass
    //the detailed notes of below 2 lines are down at the end
    private val _pieChartData = MutableStateFlow<List<PieChartData>>(emptyList())//holds the current state of pie chart data . only viewmodel can modify this value
    val pieChartData : StateFlow<List<PieChartData>> = _pieChartData.asStateFlow()//provides a readable version of _piechartdata

    private val _incomePieChartData = MutableStateFlow<List<PieChartData>>(emptyList())
    val incomePieChartData : StateFlow<List<PieChartData>> = _incomePieChartData.asStateFlow()

    fun fetchPieChartData(month: String,isExpense: Boolean){
        viewModelScope.launch{
            val data = if(isExpense){
                getPieChartDataForMonth(month)
            }else{
                getIncomePieChartDataForMonth(month)
            }
            if(isExpense){
                _pieChartData.value = data
            }else{
                _incomePieChartData.value = data
            }
        }
    }

    //suspend function to fetch pie chart data for a specifc month
    suspend fun getPieChartDataForMonth(month: String) : List<PieChartData>{

        val expenseCategories = categoryRepository.getAllCategories("Expense").first()

        val pieChartDataList = mutableListOf<PieChartData>()

        //quick fixes - toString , need to check
        for (category in expenseCategories){
            val totalSpent = expenseRepository.getTotalSpentByCategory(category.name,month) ?: 0.0
            if (totalSpent > 0.0) {
                pieChartDataList.add(
                    PieChartData(
                        category = category.name,
                        value = totalSpent,
                        color = getUniqueCategoryColor(category.name,true)
                    )
                )
            }
        }
        return pieChartDataList
    }

    suspend fun getIncomePieChartDataForMonth(month: String) : List<PieChartData>{

        val incomeCategories = categoryRepository.getAllCategories("Income").first()

        val pieChartDataList = mutableListOf<PieChartData>()

        for (category in incomeCategories){
            val totalIncome = incomeRepository.getTotalIncomeByCategory(category.name, month) ?: 0.0
            if (totalIncome>0.0) {
                pieChartDataList.add(
                    PieChartData(
                        category = category.name,
                        value = totalIncome,
                        color = getUniqueCategoryColor(category.name,false)
                    )
                )
            }
        }
        return pieChartDataList
    }
}

fun generateRandomColor(): Color{
    return Color(
        red = (50..200).random() / 255f,
        green = (50..200).random() / 255f,
        blue = (50..200).random() / 255f,
        alpha = 1f
    )
}

fun getUniqueCategoryColor(category: String,isExpenseSelected: Boolean): Color {
    // Combine both expense and income predefined colors
    val predefinedColors = categoryColors + incomeCategoryColors

    // If category exists in predefined, return it
    predefinedColors[category]?.let { return it }

    // Generate a new random color that is not in predefined
    var newColor: Color
    do {
        newColor = generateRandomColor()
    } while (newColor in predefinedColors.values) // Ensure uniqueness

    return newColor
}


// Declaring a private MutableStateFlow to hold the pie chart data.
// This is mutable and initialized with an empty list of PieChartData.
//private val _pieChartData = MutableStateFlow<List<PieChartData>>(emptyList())

// Exposing a public, read-only StateFlow version of the pie chart data.
// This allows other parts of the code (e.g., UI layer) to observe changes,
// but prevents them from modifying the state directly.
//val pieChartData: StateFlow<List<PieChartData>> = _pieChartData.asStateFlow()

/*
Explanation:
1. _pieChartData (MutableStateFlow):
   - Holds the current state of pie chart data (List<PieChartData>).
   - Only the ViewModel can modify this value to maintain encapsulation.

   Example:
   _pieChartData.value = listOf(PieChartData("Food", 150.0, Color.Red))
   This updates the MutableStateFlow with new data, which triggers updates to observers.

2. pieChartData (StateFlow):
   - Provides a read-only version of _pieChartData using asStateFlow().
   - Ensures other parts of the application can observe changes but cannot directly modify the data.

Usage:
- ViewModel updates the state:
  _pieChartData.value = newDataList

- UI observes the state:
  val pieChartData by viewModel.pieChartData.collectAsState()
  This ensures the UI reacts automatically to any changes in the data.

Why Use StateFlow?
- Reactive UI: Automatically updates the UI when the data changes.
- Encapsulation: Restricts data modification to the ViewModel.
- Modern Kotlin Approach: Integrates seamlessly with Jetpack Compose for building reactive UIs.
*/
