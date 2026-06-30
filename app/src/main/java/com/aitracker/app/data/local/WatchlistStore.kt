package com.aitracker.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "watchlist")

/** Persists the user's watchlist (set of ticker symbols) via Jetpack DataStore. */
class WatchlistStore(private val context: Context) {

    val watchedSymbols: Flow<Set<String>> = context.dataStore.data
        .map { prefs -> prefs[KEY] ?: emptySet() }

    suspend fun toggle(symbol: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY] ?: emptySet()
            prefs[KEY] = if (symbol in current) current - symbol else current + symbol
        }
    }

    suspend fun setWatched(symbol: String, watched: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY] ?: emptySet()
            prefs[KEY] = if (watched) current + symbol else current - symbol
        }
    }

    companion object {
        private val KEY = stringSetPreferencesKey("watched_symbols")
    }
}
