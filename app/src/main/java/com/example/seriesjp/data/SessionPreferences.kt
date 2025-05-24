package com.example.seriesjp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "session_prefs")

class SessionPreferences(private val context: Context) {

    companion object {
        private val KEEP_LOGGED_IN = booleanPreferencesKey("keep_logged_in")
    }

    val keepLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEEP_LOGGED_IN] ?: false }

    suspend fun setKeepLoggedIn(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEEP_LOGGED_IN] = value
        }
    }
}
