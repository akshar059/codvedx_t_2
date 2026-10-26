package com.example.codvedx_t_2.data.preferences

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("expense_tracker_prefs", Context.MODE_PRIVATE)

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    var currencySymbol: String
        get() = prefs.getString(KEY_CURRENCY_SYMBOL, "₹") ?: "₹"
        set(value) = prefs.edit().putString(KEY_CURRENCY_SYMBOL, value).apply()

    var monthlyBudget: Double
        get() = prefs.getFloat(KEY_MONTHLY_BUDGET, 30000f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_MONTHLY_BUDGET, value.toFloat()).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "User") ?: "User"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    var profileImageUri: String?
        get() = prefs.getString(KEY_PROFILE_IMAGE_URI, null)
        set(value) = prefs.edit().putString(KEY_PROFILE_IMAGE_URI, value).apply()

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        private const val KEY_MONTHLY_BUDGET = "monthly_budget"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_PROFILE_IMAGE_URI = "profile_image_uri"
    }
}
