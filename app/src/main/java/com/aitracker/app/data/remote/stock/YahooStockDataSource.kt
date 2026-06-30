package com.aitracker.app.data.remote.stock

import com.aitracker.app.data.model.StockQuote
import com.aitracker.app.data.remote.YahooFinanceApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

/** Free, no-key quote source backed by the Yahoo Finance chart endpoint. */
class YahooStockDataSource(private val api: YahooFinanceApi) : StockDataSource {

    override val providerName: String = "Yahoo Finance"

    override suspend fun getQuotes(symbols: List<String>): Map<String, StockQuote> =
        withContext(Dispatchers.IO) {
            symbols
                .map { symbol -> async { runCatching { getQuote(symbol) }.getOrNull() } }
                .awaitAll()
                .filterNotNull()
                .associateBy { it.symbol }
        }

    override suspend fun getQuote(symbol: String): StockQuote = withContext(Dispatchers.IO) {
        val response = api.getChart(symbol)
        val result = response.chart?.result?.firstOrNull()
            ?: throw RuntimeException("No data for $symbol")
        val meta = result.meta ?: throw RuntimeException("No meta for $symbol")

        val closes = result.indicators?.quote?.firstOrNull()?.close
            ?.filterNotNull()
            ?: emptyList()

        val price = meta.regularMarketPrice ?: closes.lastOrNull()
            ?: throw RuntimeException("No price for $symbol")

        val previousClose = meta.previousClose
            ?: meta.chartPreviousClose
            ?: closes.getOrNull(closes.size - 2)
            ?: price

        StockQuote(
            symbol = meta.symbol ?: symbol,
            price = price,
            previousClose = previousClose,
            currency = meta.currency ?: "USD",
            closeHistory = closes,
        )
    }
}
