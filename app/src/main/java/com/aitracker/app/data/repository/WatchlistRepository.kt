package com.aitracker.app.data.repository

import com.aitracker.app.data.local.WatchlistStore
import kotlinx.coroutines.flow.Flow

/** Thin wrapper around [WatchlistStore] so the rest of the app depends on a repository. */
class WatchlistRepository(private val store: WatchlistStore) {

    val watchedSymbols: Flow<Set<String>> = store.watchedSymbols

    suspend fun toggle(symbol: String) = store.toggle(symbol)

    suspend fun setWatched(symbol: String, watched: Boolean) = store.setWatched(symbol, watched)
}
