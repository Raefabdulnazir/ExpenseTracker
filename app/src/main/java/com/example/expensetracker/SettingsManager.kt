package com.example.expensetracker.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Centralized Settings Manager to handle app-wide settings like theme and currency
 * This class uses StateFlow to notify all screens about settings changes in real-time
 */
class SettingsManager private constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    // Currency symbols mapping - MOVED TO TOP BEFORE IT'S USED
    private val currencySymbols = mapOf(
        "USD" to "$",
        "EUR" to "€",
        "GBP" to "£",
        "JPY" to "¥",
        "CNY" to "¥",
        "INR" to "₹",
        "CAD" to "C$",
        "AUD" to "A$",
        "CHF" to "Fr",
        "KRW" to "₩",
        "RUB" to "₽",
        "BRL" to "R$",
        "MXN" to "$",
        "ZAR" to "R",
        "SGD" to "S$",
        "HKD" to "HK$",
        "NOK" to "kr",
        "SEK" to "kr",
        "DKK" to "kr",
        "PLN" to "zł",
        "AED" to "AED",
        "SAR" to "﷼",
        "QAR" to "QR",
        "KWD" to "KD",
        "BHD" to "BD",
        "OMR" to "OMR",
        "JOD" to "JD",
        "LBP" to "LL",
        "EGP" to "E£",
        "ILS" to "₪",
        "IRR" to "﷼",
        "IQD" to "ID",
        "TRY" to "₺",
        "THB" to "฿",
        "MYR" to "RM",
        "IDR" to "Rp",
        "PHP" to "₱",
        "VND" to "₫",
        "PKR" to "Rs",
        "BDT" to "৳",
        "LKR" to "Rs",
        "NPR" to "Rs",
        "AFN" to "؋"
    )

    // Currency Settings
    private val _currencyCode = MutableStateFlow(
        sharedPrefs.getString("currency_code", "USD") ?: "USD"
    )
    val currencyCode: StateFlow<String> = _currencyCode.asStateFlow()

    private val _currencySymbol = MutableStateFlow(getCurrencySymbol(_currencyCode.value))
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    // Theme Settings
    private val _isDarkMode = MutableStateFlow(
        sharedPrefs.getBoolean("dark_mode", true)
    )
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Notification Settings
    private val _notificationsEnabled = MutableStateFlow(
        sharedPrefs.getBoolean("notifications", true)
    )
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    /**
     * Update currency settings
     */
    fun updateCurrency(currencyCode: String) {
        _currencyCode.value = currencyCode
        _currencySymbol.value = getCurrencySymbol(currencyCode)

        // Save to SharedPreferences
        sharedPrefs.edit()
            .putString("currency_code", currencyCode)
            .apply()
    }

    /**
     * Update theme settings
     */
    fun updateTheme(isDarkMode: Boolean) {
        _isDarkMode.value = isDarkMode

        // Save to SharedPreferences
        sharedPrefs.edit()
            .putBoolean("dark_mode", isDarkMode)
            .apply()
    }

    /**
     * Update notification settings
     */
    fun updateNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled

        // Save to SharedPreferences
        sharedPrefs.edit()
            .putBoolean("notifications", enabled)
            .apply()
    }

    /**
     * Get currency symbol for given currency code
     */
    private fun getCurrencySymbol(currencyCode: String): String {
        return currencySymbols[currencyCode] ?: "$"
    }

    /**
     * Format amount with current currency symbol
     */
    fun formatAmount(amount: Double): String {
        return "${_currencySymbol.value}${"%.2f".format(amount)}"
    }

    /**
     * Get available currencies
     */
    fun getAvailableCurrencies(): List<String> {
        return listOf(
            "USD - US Dollar",
            "EUR - Euro",
            "GBP - British Pound",
            "JPY - Japanese Yen",
            "CNY - Chinese Yuan",
            "INR - Indian Rupee",
            "CAD - Canadian Dollar",
            "AUD - Australian Dollar",
            "CHF - Swiss Franc",
            "KRW - South Korean Won",
            "RUB - Russian Ruble",
            "BRL - Brazilian Real",
            "MXN - Mexican Peso",
            "ZAR - South African Rand",
            "SGD - Singapore Dollar",
            "HKD - Hong Kong Dollar",
            "NOK - Norwegian Krone",
            "SEK - Swedish Krona",
            "DKK - Danish Krone",
            "PLN - Polish Zloty",
            "AED - UAE Dirham",
            "SAR - Saudi Riyal",
            "QAR - Qatari Riyal",
            "KWD - Kuwaiti Dinar",
            "BHD - Bahraini Dinar",
            "OMR - Omani Rial",
            "JOD - Jordanian Dinar",
            "LBP - Lebanese Pound",
            "EGP - Egyptian Pound",
            "ILS - Israeli Shekel",
            "IRR - Iranian Rial",
            "IQD - Iraqi Dinar",
            "TRY - Turkish Lira",
            "THB - Thai Baht",
            "MYR - Malaysian Ringgit",
            "IDR - Indonesian Rupiah",
            "PHP - Philippine Peso",
            "VND - Vietnamese Dong",
            "PKR - Pakistani Rupee",
            "BDT - Bangladeshi Taka",
            "LKR - Sri Lankan Rupee",
            "NPR - Nepalese Rupee",
            "AFN - Afghan Afghani"
        )
    }

    /**
     * Get currency display name from code
     */
    fun getCurrencyDisplayName(currencyCode: String): String {
        return getAvailableCurrencies().find { it.startsWith(currencyCode) } ?: "USD - US Dollar"
    }

    /**
     * Extract currency code from display string
     */
    fun extractCurrencyCode(currencyDisplay: String): String {
        return currencyDisplay.substring(0, 3)
    }
}