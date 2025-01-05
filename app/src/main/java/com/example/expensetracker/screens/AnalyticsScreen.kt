package com.example.expensetracker.screens

import android.os.Build
import android.util.Log
import android.widget.Space
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.expensetracker.model.PieChartData
import com.example.expensetracker.viewmodel.AnalyticsViewModel
import org.w3c.dom.Text
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    currentMonth: String
    //onMonthChange: (String) -> Unit
){
    var currentMonthForSelector by remember {
        mutableStateOf(LocalDate.now())
    }

    var selectedMonth = currentMonthForSelector.toString().substring(0, 7)  // Format to "YYYY-MM"
    var isExpenseSelected by remember { mutableStateOf(true) }  //tracks toggle state

    //Observe the Stateflow data from the Viewmodel
    val pieChartData by viewModel.pieChartData.collectAsState(initial = emptyList())
    val incomePieChartData by viewModel.incomePieChartData.collectAsState(initial = emptyList())

    //trigger data change when month changes
    LaunchedEffect(selectedMonth,isExpenseSelected) {
        try{
            val formattedMonth = selectedMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            viewModel.fetchPieChartData (formattedMonth,isExpenseSelected)
        }
        catch (e:Exception){
            Log.e("AnalyticScreen","Error fetching pie chart data")
        }
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        monthSelector(
            currentMonth = currentMonthForSelector,
            onPreviousMonth = { currentMonthForSelector = currentMonthForSelector.minusMonths(1) },
            onNextMonth = { currentMonthForSelector = currentMonthForSelector.plusMonths(1) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OverviewToggle(isExpenseSelected)   {isExpenseSelected = it}//overview toggle for income and expense
        //this lambda updates the isExpenseSelected when the button is clicked
        //'it' is the boolean value passed from button click
        //      true for Expense
        //      false for Income

        Spacer(modifier = Modifier.height(16.dp))

        val chartData = if(isExpenseSelected) pieChartData else incomePieChartData

        //piechart
        if(chartData.isNotEmpty()){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ){
                PieChart(chartData)
            }
        }else{
            Text(text = "Loading data ...", style = MaterialTheme.typography.bodyMedium)
        }

        //category breakdown
        chartData.forEach { data ->
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Box (
                    modifier = Modifier
                        .size(16.dp)
                        .background(color = data.color, shape = CircleShape)
                )//{
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${data.category}")
                //}
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        //list of categories and its amount and percentage
        chartData.forEach { data ->
            val percentage = (data.value/chartData.sumOf { it.value } * 100).toInt()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = data.category, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "${data.value} (${percentage}%)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

}

@Composable
fun OverviewToggle(
    isExpenseSelected: Boolean,
    onToggle: (Boolean) -> Unit //this is a callback that informs the parent composable(AnalyticsScreen) of the toggle's state change(Expense = true/Income = false)
){

    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ){
        Button(
            onClick = { onToggle(true) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isExpenseSelected) MaterialTheme.colorScheme.primary else Color.LightGray
            )
        ) {
            Text(text = "Expenses")
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = { onToggle(false) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if(!isExpenseSelected) MaterialTheme.colorScheme.primary else Color.LightGray
            )
        ) {
            Text(text = "Income")
        }
    }
}

@Composable
fun PieChart(pieChartData: List<PieChartData>){
    val totalValue = pieChartData.sumOf { it.value }
    val padding = 16.dp
    val canvasSize = 300.dp
    var startAngle = 0f

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .size(canvasSize)){ //Canvas composable is to draw graphics like arcs of a piegraph
        val diameter = size.minDimension - padding.toPx() * 2
        val topLeft = Offset(
            (size.width - diameter) / 2,
            (size.height - diameter) / 2
        )
        //}

        pieChartData.forEach { data ->
            val sweepAngle = (data.value/totalValue*360).toFloat()//to determine the proportional sweep angle for each category
            drawArc(
                color = data.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,   //closes the arc by connecting it to the center(forming a pie slice)
                topLeft = topLeft /*rect.topLeft*/, //top left corner of the rectangle
                size = androidx.compose.ui.geometry.Size(diameter, diameter)/*rect.size*/    //size of the rectangle(defines the diameter of the arc)
            )
            startAngle+=sweepAngle
        }
    }
}

//need to refer this function again
private fun DrawScope.drawPieSlice(startAngle: Float , sweepAngle: Float , color: Color){
    val diameter = size.minDimension
    val padding = 16f
    val rect = Rect(Offset(padding, padding), Offset(diameter -padding, diameter - padding))

    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = rect.topLeft,
        size = rect.size
    )
}