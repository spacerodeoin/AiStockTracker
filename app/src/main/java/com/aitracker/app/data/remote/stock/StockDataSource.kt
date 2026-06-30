package com.aitracker.app.data.remote.stock

import com.aitracker.app.data.model.StockQuote

/** A swappable source of market quotes (Yahoo Finance free, or keyed providers like Finnhub). */
interface StockDataSource {

    /** Human-readable provider name, useful for diagnostics. */
    val providerName: String

    suspend fun getQuote(symbol: String): StockQuote

    /** Fetch many symbols in parallel; symbols that fail are omitted from the result. */
    suspend fun getQuotes(symbols: List<String>): Map<String, StockQuote>
}
