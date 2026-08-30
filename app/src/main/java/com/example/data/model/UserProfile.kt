package com.example.data.model

data class CurrencyInfo(
    val code: String,
    val symbol: String,
    val name: String
)

val SupportedCurrencies = listOf(
    CurrencyInfo("INR", "₹", "Indian Rupee (INR)"),
    CurrencyInfo("USD", "$", "US Dollar (USD)"),
    CurrencyInfo("EUR", "€", "Euro (EUR)"),
    CurrencyInfo("GBP", "£", "British Pound (GBP)"),
    CurrencyInfo("AED", "AED", "UAE Dirham (AED)"),
    CurrencyInfo("CAD", "C$", "Canadian Dollar (CAD)"),
    CurrencyInfo("AUD", "A$", "Australian Dollar (AUD)"),
    CurrencyInfo("SGD", "S$", "Singapore Dollar (SGD)"),
    CurrencyInfo("JPY", "¥", "Japanese Yen (JPY)")
)

data class UserProfile(
    val userName: String = "John Doe",
    val profileImage: String = "default",
    val currencyCode: String = "INR",
    val currencySymbol: String = "₹",
    val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val notificationsEnabled: Boolean = true,
    val budgetAlertsEnabled: Boolean = true,
    val monthlyRemindersEnabled: Boolean = true,
    val goalRemindersEnabled: Boolean = true,
    val isOnboardingCompleted: Boolean = false
)
