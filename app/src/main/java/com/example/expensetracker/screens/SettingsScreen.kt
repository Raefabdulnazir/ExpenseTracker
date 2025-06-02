package com.example.expensetracker.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.utils.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager
) {
    // Observe settings from SettingsManager using StateFlow
    val isDarkMode by settingsManager.isDarkMode.collectAsState()
    val selectedCurrencyCode by settingsManager.currencyCode.collectAsState()
    val currencySymbol by settingsManager.currencySymbol.collectAsState()
    val notificationsEnabled by settingsManager.notificationsEnabled.collectAsState()

    var showCurrencyDialog by remember { mutableStateOf(false) }

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
                    onCheckedChange = { newValue ->
                        settingsManager.updateTheme(newValue)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Currency Preference
            SettingsRow(
                title = "Currency",
                subtitle = "$currencySymbol - ${settingsManager.getCurrencyDisplayName(selectedCurrencyCode).substring(6)}"
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
                    onCheckedChange = { newValue ->
                        settingsManager.updateNotifications(newValue)
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
            currencies = settingsManager.getAvailableCurrencies(),
            selectedCurrencyCode = selectedCurrencyCode,
            onCurrencySelected = { currencyDisplay ->
                val newCurrencyCode = settingsManager.extractCurrencyCode(currencyDisplay)
                settingsManager.updateCurrency(newCurrencyCode)
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
    selectedCurrencyCode: String,
    onCurrencySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Select Currency") },
        text = {
            Column(
                modifier = Modifier
                    .height(400.dp) // Set a fixed height for the dialog content
                    .verticalScroll(rememberScrollState()) // Make it scrollable
            ) {
                currencies.forEach { currency ->
                    val currencyCode = currency.substring(0, 3)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onCurrencySelected(currency) }, // Make entire row clickable
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currencyCode == selectedCurrencyCode,
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