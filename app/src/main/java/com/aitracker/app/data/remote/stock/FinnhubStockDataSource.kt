package com.aitracker.app.data.remote.stock

import com.aitracker.app.data.model.StockQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

/**
 * Keyed quote source backed by Finnhub. The free quote endpoint returns the latest price and
 * previous close but no intraday history, so [StockQuote.closeHistory] is left empty (the UI
 * hides the sparkline / chart gracefully when history is unavailable).
 */
class FinnhubStockDataSource(
    private val api: FinnhubApi,
    private val apiKey: String,
) : StockDataSource {

    override val providerName: String = "Finnhub"

    override suspend fun getQuotes(symbols: List<String>): Map<String, StockQuote> =
        withContext(Dispatchers.IO) {
            symbols
                .map { symbol -> async { runCatching { getQuote(symbol) }.getOrNull() } }
                .awaitAll()
                .filterNotNull()
                .associateBy { it.symbol }
        }

    override suspend fun getQuote(symbol: String): StockQuote = withContext(Dispatchers.IO) {
        val dto = api.getQuote(symbol, apiKey)
        val price = dto.current?.takeIf { it > 0.0 }
            ?: throw RuntimeException("No price for $symbol")
        StockQuote(
            symbol = symbol,
            price = price,
            previousClose = dto.previousClose ?: price,
            currency = "USD",
            closeHistory = emptyList(),
        )
    }
}
