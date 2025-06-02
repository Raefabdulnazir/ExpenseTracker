package com.example.expensetracker.screens

import android.os.Build
import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.model.PieChartData
import com.example.expensetracker.viewmodel.AnalyticsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.expensetracker.formatAmount
import com.example.expensetracker.utils.SettingsManager

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    currentMonth: String,
    settingsManager: SettingsManager
    ) {
    var currentMonthForSelector by remember { mutableStateOf(LocalDate.now()) }
    var selectedMonth = currentMonthForSelector.toString().substring(0, 7)
    var isExpenseSelected by remember { mutableStateOf(true) }

    // Observe settings from SettingsManager
    val currencySymbol by settingsManager.currencySymbol.collectAsState()
    val currencyCode by settingsManager.currencyCode.collectAsState()

    // Observe the StateFlow data from the ViewModel
    val pieChartData by viewModel.pieChartData.collectAsState(initial = emptyList())
    val incomePieChartData by viewModel.incomePieChartData.collectAsState(initial = emptyList())

    // Trigger data change when month changes
    LaunchedEffect(selectedMonth, isExpenseSelected) {
        try {
            val formattedMonth = selectedMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            viewModel.fetchPieChartData(formattedMonth, isExpenseSelected)
        } catch (e: Exception) {
            Log.e("AnalyticScreen", "Error fetching pie chart data")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Month Selector
        MonthSelector(
            currentMonth = currentMonthForSelector,
            onPreviousMonth = { currentMonthForSelector = currentMonthForSelector.minusMonths(1) },
            onNextMonth = { currentMonthForSelector = currentMonthForSelector.plusMonths(1) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle Section
        ToggleSection(
            isExpenseSelected = isExpenseSelected,
            onToggle = { isExpenseSelected = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main Content
        val chartData = if (isExpenseSelected) pieChartData else incomePieChartData

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Chart Section
            item {
                ChartSection(chartData = chartData)
            }

            // Breakdown Section
            if (chartData.isNotEmpty()) {
                item {
                    BreakdownSection(chartData = chartData, currencySymbol = currencySymbol)
                }
            }
        }
    }
}

@Composable
private fun ToggleSection(
    isExpenseSelected: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onToggle(true) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isExpenseSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        Color.LightGray.copy(alpha = 0.3f),
                    contentColor = if (isExpenseSelected)
                        Color.White
                    else
                        MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Expenses",
                    fontWeight = if (isExpenseSelected) FontWeight.Bold else FontWeight.Normal
                )
            }

            Button(
                onClick = { onToggle(false) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isExpenseSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        Color.LightGray.copy(alpha = 0.3f),
                    contentColor = if (!isExpenseSelected)
                        Color.White
                    else
                        MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Income",
                    fontWeight = if (!isExpenseSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun ChartSection(chartData: List<PieChartData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Category Distribution",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (chartData.isNotEmpty()) {
                // Pie Chart
                Box(
                    modifier = Modifier.size(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PieChart(chartData)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Legend
                Legend(chartData = chartData)
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No Data Available",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add some transactions to see analytics",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun Legend(chartData: List<PieChartData>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chartData.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                pair.forEach { data ->
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(color = data.color, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = data.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
                // Fill remaining space if odd number of items
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun BreakdownSection(chartData: List<PieChartData>,currencySymbol: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Category Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            chartData.forEach { data ->
                val percentage = (data.value / chartData.sumOf { it.value } * 100)
                val formattedPercentage = "%.2f".format(percentage)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp), // Reduced padding
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side: Color dot and category name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f) // Takes available space
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(color = data.color, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = data.category,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Right side: Amount and percentage (no extra spacing)
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "$currencySymbol${(data.value).formatAmount()}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$formattedPercentage%",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }

                // Optional: Add a subtle divider between items (except for the last item)
                if (data != chartData.last()) {
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

@Composable
private fun PieChart(pieChartData: List<PieChartData>) {
    val totalValue = pieChartData.sumOf { it.value }
    var startAngle = 0f

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.8f

        pieChartData.forEach { data ->
            val sweepAngle = (data.value / totalValue * 360).toFloat()

            drawArc(
                color = data.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2)
            )
            startAngle += sweepAngle
        }
    }
}