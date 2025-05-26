package com.example.expensetracker.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    //onBackClick: () -> Unit
) {

    val context = LocalContext.current  //way to access android system features
    val sharedPrefs = remember {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)  //creates/opens a storage file named "app_settings" in private mode(means only our app can use this file)
    }

    // State variables for settings - initialize from SharedPreferences
    var isDarkMode by remember { mutableStateOf(sharedPrefs.getBoolean("dark_mode",false)) }
    var selectedCurrency by remember { mutableStateOf(sharedPrefs.getString("currency","$")?: "$") }
    var notificationsEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("notifications",true)) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    // Available currencies
    val currencies = listOf("$", "€", "£", "¥", "₹", "₽", "₩", "₨")

    //Function to save settings
    fun saveSettings(){
        with(sharedPrefs.edit()) {
            putBoolean("dark_mode",isDarkMode)
            putString("currency",selectedCurrency)
            putBoolean("notifications",notificationsEnabled)
            apply() //Use apply() for async save, commit() for sync save
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
/*            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },*/
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Settings Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // APPEARANCE Section
            SectionHeader(title = "APPEARANCE")
            Spacer(modifier = Modifier.height(8.dp))

            // Dark/Light Mode Toggle
            SettingsRow(
                title = "Dark Mode",
                subtitle = if (isDarkMode) "Dark theme enabled" else "Light theme enabled"
            ) {
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = {
                        isDarkMode = it
                        saveSettings()
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Currency Preference
            SettingsRow(
                title = "Currency",
                subtitle = "Selected: $selectedCurrency"
            ) {
                TextButton(
                    onClick = { showCurrencyDialog = true }
                ) {
                    Text(text = "Change")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // NOTIFICATIONS Section
            SectionHeader(title = "NOTIFICATIONS")
            Spacer(modifier = Modifier.height(8.dp))

            // Enable/Disable Notifications
            SettingsRow(
                title = "Enable Notifications",
                subtitle = if (notificationsEnabled) "Notifications are turned on" else "Notifications are turned off"
            ) {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = {
                        notificationsEnabled = it
                        saveSettings()
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ABOUT APP Section
            SectionHeader(title = "ABOUT APP")
            Spacer(modifier = Modifier.height(8.dp))

            // App Version
            SettingsRow(
                title = "Version",
                subtitle = "1.0.0"
            ) {
                // Empty - just for display
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Developer Info
            SettingsRow(
                title = "Developer",
                subtitle = "Built with ❤️ for expense tracking"
            ) {
                // Empty - just for display
            }
        }
    }

    // Currency Selection Dialog
    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            currencies = currencies,
            selectedCurrency = selectedCurrency,
            onCurrencySelected = { currency ->
                selectedCurrency = currency
                saveSettings() // Save when currency is selected
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String,
    trailingContent: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            trailingContent()
        }
    }
}

@Composable
fun CurrencySelectionDialog(
    currencies: List<String>,
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Select Currency") },
        text = {
            Column {
                currencies.forEach { currency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currency == selectedCurrency,
                            onClick = { onCurrencySelected(currency) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = currency, fontSize = 16.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}