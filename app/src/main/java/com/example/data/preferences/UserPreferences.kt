package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nmm_user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        val USER_PIN = stringPreferencesKey("user_pin")
        val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val SELECTED_CURRENCY = stringPreferencesKey("selected_currency")
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val INCOME_CATEGORIES = stringPreferencesKey("income_categories")
        val EXPENSE_CATEGORIES = stringPreferencesKey("expense_categories")
        val IS_AUTO_BACKUP_ENABLED = booleanPreferencesKey("is_auto_backup_enabled")
    }

    val userPinFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_PIN]
    }

    val isBiometricEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_BIOMETRIC_ENABLED] ?: false
    }

    val isDarkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_DARK_MODE] ?: true // Dark premium standard layout
    }

    val selectedCurrencyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_CURRENCY] ?: "Rp"
    }

    val selectedLanguageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_LANGUAGE] ?: "ID"
    }

    val hasCompletedOnboardingFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAS_COMPLETED_ONBOARDING] ?: false
    }

    val isAutoBackupEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_AUTO_BACKUP_ENABLED] ?: false
    }

    val incomeCategoriesFlow: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val catsStr = preferences[INCOME_CATEGORIES] ?: "Gaji,Freelance,Bonus,Investasi,Lainnya"
        catsStr.split(",").filter { it.isNotBlank() }
    }

    val expenseCategoriesFlow: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val catsStr = preferences[EXPENSE_CATEGORIES] ?: "Makanan,Transportasi,Belanja,Hiburan,Pendidikan,Kesehatan,Tagihan,Lainnya"
        catsStr.split(",").filter { it.isNotBlank() }
    }

    suspend fun setUserPin(pin: String?) {
        context.dataStore.edit { preferences ->
            if (pin == null) {
                preferences.remove(USER_PIN)
            } else {
                preferences[USER_PIN] = pin
            }
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setDarkMode(darkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = darkMode
        }
    }

    suspend fun setSelectedCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_CURRENCY] = currency
        }
    }

    suspend fun setSelectedLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_LANGUAGE] = language
        }
    }

    suspend fun setCompletedOnboarding(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAS_COMPLETED_ONBOARDING] = completed
        }
    }

    suspend fun saveIncomeCategories(categories: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[INCOME_CATEGORIES] = categories.joinToString(",")
        }
    }

    suspend fun saveExpenseCategories(categories: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[EXPENSE_CATEGORIES] = categories.joinToString(",")
        }
    }

    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_AUTO_BACKUP_ENABLED] = enabled
        }
    }

    suspend fun clearAllPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
