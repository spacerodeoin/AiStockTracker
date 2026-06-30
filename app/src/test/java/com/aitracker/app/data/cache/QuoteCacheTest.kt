package com.aitracker.app.data.cache

import com.aitracker.app.data.model.StockQuote
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuoteCacheTest {

    private var now = 0L
    private val cache = QuoteCache(ttlMillis = 1_000L, clock = { now })

    private fun quote(symbol: String, price: Double = 100.0) =
        StockQuote(
            symbol = symbol,
            price = price,
            previousClose = 90.0,
            currency = "USD",
            closeHistory = listOf(90.0, price),
        )

    @Test
    fun `returns cached quote within ttl`() {
        cache.put(quote("NVDA"))
        now = 999L
        assertEquals(100.0, cache.get("NVDA")?.price)
    }

    @Test
    fun `caches the price history`() {
        cache.put(quote("NVDA", price = 120.0))
        assertEquals(listOf(90.0, 120.0), cache.get("NVDA")?.closeHistory)
    }

    @Test
    fun `returns null once the entry is older than ttl`() {
        cache.put(quote("NVDA"))
        now = 1_000L // exactly at TTL boundary is considered stale
        assertNull(cache.get("NVDA"))
    }

    @Test
    fun `getFresh returns only the fresh subset`() {
        cache.put(quote("NVDA")) // stamped at t=0
        now = 1_001L
        cache.put(quote("AMD")) // stamped at t=1001

        val fresh = cache.getFresh(listOf("NVDA", "AMD"))

        assertEquals(setOf("AMD"), fresh.keys)
        assertEquals(100.0, fresh["AMD"]?.price)
    }

    @Test
    fun `get returns null for unknown symbol`() {
        assertNull(cache.get("UNKNOWN"))
    }

    @Test
    fun `clear evicts all entries`() {
        cache.put(quote("NVDA"))
        cache.clear()
        assertNull(cache.get("NVDA"))
    }
}
