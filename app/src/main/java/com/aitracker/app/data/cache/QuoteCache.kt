package com.aitracker.app.data.cache

import com.aitracker.app.data.model.StockQuote
import java.util.concurrent.ConcurrentHashMap

/**
 * Simple thread-safe, in-memory TTL cache for stock quotes (including their price history).
 *
 * Entries older than [ttlMillis] are treated as stale and ignored on read, so the UI can
 * switch tabs or reopen the detail screen without re-hitting the network, while a forced
 * refresh still bypasses the cache. The [clock] is injectable to keep the cache unit-testable.
 */
class QuoteCache(
    private val ttlMillis: Long = DEFAULT_TTL_MILLIS,
    private val clock: () -> Long = System::currentTimeMillis,
) {

    private data class Entry(val quote: StockQuote, val timestamp: Long)

    private val entries = ConcurrentHashMap<String, Entry>()

    /** Returns the cached quote for [symbol] only if it is still fresh. */
    fun get(symbol: String): StockQuote? =
        entries[symbol]?.takeIf { it.isFresh() }?.quote

    /** Returns only the fresh subset of [symbols] currently cached. */
    fun getFresh(symbols: List<String>): Map<String, StockQuote> =
        symbols.mapNotNull { symbol -> get(symbol)?.let { symbol to it } }.toMap()

    fun put(quote: StockQuote) {
        entries[quote.symbol] = Entry(quote, clock())
    }

    fun putAll(quotes: Collection<StockQuote>) {
        quotes.forEach(::put)
    }

    fun clear() = entries.clear()

    private fun Entry.isFresh(): Boolean = clock() - timestamp < ttlMillis

    companion object {
        const val DEFAULT_TTL_MILLIS = 60_000L
    }
}
