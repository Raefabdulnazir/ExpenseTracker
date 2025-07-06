package com.moneynest.expensetracker.ui.theme

import androidx.compose.ui.graphics.Color

//this file is created for mapping colours to the fields in analysis graph

val categoryColors = mapOf(
    "House/Rent" to Color(0xFF7B68EE),
    "Healthcare" to Color(0xFF00BFFF),
    "Shopping" to Color(0xFFFF69B4),
    "Personal Care" to Color(0xFFFFA07A),
    "Education" to Color(0xFF4682B4),
    "Food" to Color(0xFFFFD700),
    "Groceries" to Color(0xFF32CD32),
    "Entertainment" to Color(0xFFFF4500),
    "Transportation" to Color(0xFF708090),
    "Utilities" to Color(0xFF8A2BE2),
    "Other" to Color(0xFFB0C4DE)
)

val incomeCategoryColors = mapOf(
    "Salary" to Color(0xFF4682B4), // Steel Blue for stability and reliability
    "Side-income" to Color(0xFF32CD32), // Lime Green for growth and initiative
    "Business" to Color(0xFFDAA520), // Goldenrod for wealth and opportunity
    "Rewards" to Color(0xFFFFA500), // Orange for positivity and achievement
    "Others" to Color(0xFFB0C4DE)  // Light Steel Blue for neutrality and miscellaneous
)