package com.rajsv.exitsense.data.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension to create DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsDataStore {

    // Keys
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications")
    private val LOCATION_SERVICES_KEY = booleanPreferencesKey("location_services")
    private val SMART_REMINDERS_KEY = booleanPreferencesKey("smart_reminders")
    private val REMINDER_SENSITIVITY_KEY = intPreferencesKey("reminder_sensitivity")
    private val LANGUAGE_KEY = stringPreferencesKey("language")
    private val APP_LOCK_KEY = booleanPreferencesKey("app_lock")

    // Get Dark Mode
    fun getDarkMode(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[DARK_MODE_KEY] ?: true // Default: true (dark mode on)
        }
    }

    // Set Dark Mode
    suspend fun setDarkMode(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    // Get Notifications
    fun getNotifications(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[NOTIFICATIONS_KEY] ?: true // Default: true
        }
    }

    // Set Notifications
    suspend fun setNotifications(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_KEY] = enabled
        }
    }

    // Get Location Services
    fun getLocationServices(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[LOCATION_SERVICES_KEY] ?: true // Default: true
        }
    }

    // Set Location Services
    suspend fun setLocationServices(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[LOCATION_SERVICES_KEY] = enabled
        }
    }

    // Get Smart Reminders
    fun getSmartReminders(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[SMART_REMINDERS_KEY] ?: true // Default: true
        }
    }

    // Set Smart Reminders
    suspend fun setSmartReminders(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SMART_REMINDERS_KEY] = enabled
        }
    }

    // Get Reminder Sensitivity (0 = Low, 1 = Medium, 2 = High)
    fun getReminderSensitivity(context: Context): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[REMINDER_SENSITIVITY_KEY] ?: 1 // Default: Medium
        }
    }

    // Set Reminder Sensitivity
    suspend fun setReminderSensitivity(context: Context, level: Int) {
        context.dataStore.edit { preferences ->
            preferences[REMINDER_SENSITIVITY_KEY] = level
        }
    }

    // Get Language
    fun getLanguage(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[LANGUAGE_KEY] ?: "English" // Default: English
        }
    }

    // Set Language
    suspend fun setLanguage(context: Context, language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    // Get App Lock
    fun getAppLock(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[APP_LOCK_KEY] ?: false // Default: false
        }
    }

    // Set App Lock
    suspend fun setAppLock(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[APP_LOCK_KEY] = enabled
        }
    }
}