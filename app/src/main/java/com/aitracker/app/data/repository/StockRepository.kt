package com.aitracker.app.data.repository

import com.aitracker.app.data.cache.QuoteCache
import com.aitracker.app.data.model.StockQuote
import com.aitracker.app.data.remote.stock.StockDataSource

/**
 * Loads market quotes for AI companies from the configured [StockDataSource], serving fresh
 * results from an in-memory [QuoteCache] to avoid redundant network calls. A forced refresh
 * bypasses the cache.
 */
class StockRepository(
    private val dataSource: StockDataSource,
    private val cache: QuoteCache = QuoteCache(),
) {

    val providerName: String get() = dataSource.providerName

    suspend fun getQuotes(
        symbols: List<String>,
        forceRefresh: Boolean = false,
    ): Map<String, StockQuote> {
        val cached = if (forceRefresh) emptyMap() else cache.getFresh(symbols)
        val missing = symbols.filter { it !in cached }
        if (missing.isEmpty()) return cached

        val fetched = dataSource.getQuotes(missing)
        cache.putAll(fetched.values)
        return cached + fetched
    }

    suspend fun getQuote(symbol: String, forceRefresh: Boolean = false): StockQuote {
        if (!forceRefresh) {
            cache.get(symbol)?.let { return it }
        }
        return dataSource.getQuote(symbol).also { cache.put(it) }
    }
}
