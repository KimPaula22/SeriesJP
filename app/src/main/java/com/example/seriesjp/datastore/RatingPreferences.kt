package com.example.seriesjp.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "ratings")

class RatingPreferences(private val context: Context) {

    companion object {
        private fun ratingKey(seriesId: Int) = intPreferencesKey("rating_$seriesId")
    }

    suspend fun saveRating(seriesId: Int, rating: Int) {
        context.dataStore.edit { preferences ->
            preferences[ratingKey(seriesId)] = rating
        }
    }

    fun getRating(seriesId: Int): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[ratingKey(seriesId)] ?: 0
        }
    }
}
