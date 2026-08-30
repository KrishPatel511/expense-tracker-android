package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("expense_tracker_prefs", Context.MODE_PRIVATE)

    private val _userProfileFlow = MutableStateFlow(loadUserProfile())
    val userProfileFlow: StateFlow<UserProfile> = _userProfileFlow.asStateFlow()

    private fun loadUserProfile(): UserProfile {
        return UserProfile(
            userName = prefs.getString("user_name", "John Doe") ?: "John Doe",
            profileImage = prefs.getString("profile_image", "default") ?: "default",
            currencyCode = prefs.getString("currency_code", "INR") ?: "INR",
            currencySymbol = prefs.getString("currency_symbol", "₹") ?: "₹",
            themeMode = prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM",
            notificationsEnabled = prefs.getBoolean("notifications_enabled", true),
            budgetAlertsEnabled = prefs.getBoolean("budget_alerts_enabled", true),
            monthlyRemindersEnabled = prefs.getBoolean("monthly_reminders_enabled", true),
            goalRemindersEnabled = prefs.getBoolean("goal_reminders_enabled", true),
            isOnboardingCompleted = prefs.getBoolean("onboarding_completed", false)
        )
    }

    fun updateUserProfile(profile: UserProfile) {
        prefs.edit().apply {
            putString("user_name", profile.userName)
            putString("profile_image", profile.profileImage)
            putString("currency_code", profile.currencyCode)
            putString("currency_symbol", profile.currencySymbol)
            putString("theme_mode", profile.themeMode)
            putBoolean("notifications_enabled", profile.notificationsEnabled)
            putBoolean("budget_alerts_enabled", profile.budgetAlertsEnabled)
            putBoolean("monthly_reminders_enabled", profile.monthlyRemindersEnabled)
            putBoolean("goal_reminders_enabled", profile.goalRemindersEnabled)
            putBoolean("onboarding_completed", profile.isOnboardingCompleted)
            apply()
        }
        _userProfileFlow.value = profile
    }

    fun setOnboardingCompleted(completed: Boolean) {
        val current = _userProfileFlow.value
        updateUserProfile(current.copy(isOnboardingCompleted = completed))
    }

    fun setCurrency(code: String, symbol: String) {
        val current = _userProfileFlow.value
        updateUserProfile(current.copy(currencyCode = code, currencySymbol = symbol))
    }

    fun setThemeMode(mode: String) {
        val current = _userProfileFlow.value
        updateUserProfile(current.copy(themeMode = mode))
    }

    fun setUserName(name: String) {
        val current = _userProfileFlow.value
        updateUserProfile(current.copy(userName = name))
    }

    fun clearAllPreferences() {
        prefs.edit().clear().apply()
        _userProfileFlow.value = UserProfile(isOnboardingCompleted = true)
    }
}
