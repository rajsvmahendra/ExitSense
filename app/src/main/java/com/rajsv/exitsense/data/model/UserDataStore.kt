package com.rajsv.exitsense.data.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_data")

object UserDataStore {

    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val USER_NAME = stringPreferencesKey("user_name")
    private val USER_EMAIL = stringPreferencesKey("user_email")
    private val USER_PHOTO_URL = stringPreferencesKey("user_photo_url")
    private val IS_FIRST_LOGIN = booleanPreferencesKey("is_first_login")

    // Check if user is logged in
    fun isLoggedIn(context: Context): Flow<Boolean> {
        return context.userDataStore.data.map { preferences ->
            preferences[IS_LOGGED_IN] ?: false
        }
    }

    // Get user name
    fun getUserName(context: Context): Flow<String> {
        return context.userDataStore.data.map { preferences ->
            preferences[USER_NAME] ?: ""
        }
    }

    // Get user email
    fun getUserEmail(context: Context): Flow<String> {
        return context.userDataStore.data.map { preferences ->
            preferences[USER_EMAIL] ?: ""
        }
    }

    // Get user photo URL
    fun getUserPhotoUrl(context: Context): Flow<String?> {
        return context.userDataStore.data.map { preferences ->
            preferences[USER_PHOTO_URL]
        }
    }

    // Login user
    suspend fun login(context: Context, name: String, email: String, photoUrl: String? = null) {
        context.userDataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USER_NAME] = name
            preferences[USER_EMAIL] = email
            photoUrl?.let { preferences[USER_PHOTO_URL] = it }
        }
    }

    // Set first login complete
    suspend fun setFirstLoginComplete(context: Context) {
        context.userDataStore.edit { preferences ->
            preferences[IS_FIRST_LOGIN] = false
        }
    }

    // Logout user
    suspend fun logout(context: Context) {
        context.userDataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = false
            preferences[USER_NAME] = ""
            preferences[USER_EMAIL] = ""
        }
    }

    // Clear all user data
    suspend fun clearAllData(context: Context) {
        context.userDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}