package com.aitracker.app.ui.stocks

import com.aitracker.app.data.model.AiCompanies
import com.aitracker.app.data.model.AiCompanyQuote
import com.aitracker.app.data.model.StockQuote
import com.aitracker.app.data.remote.realtime.PriceTick
import org.junit.Assert.assertEquals
import org.junit.Test

class ApplyTickTest {

    private fun quote(symbol: String, price: Double) = StockQuote(
        symbol = symbol,
        price = price,
        previousClose = 100.0,
        currency = "USD",
        closeHistory = emptyList(),
    )

    private fun item(symbol: String, price: Double) =
        AiCompanyQuote(company = AiCompanies.bySymbol(symbol)!!, quote = quote(symbol, price))

    @Test
    fun `updates only the target symbol price and preserves previousClose`() {
        val items = listOf(item("NVDA", 100.0), item("AMD", 50.0))

        val result = applyTick(items, PriceTick("NVDA", 111.0, 0L))

        val nvda = result.first { it.company.symbol == "NVDA" }.quote!!
        assertEquals(111.0, nvda.price, 0.0)
        assertEquals(100.0, nvda.previousClose, 0.0) // change% recomputes from this
        // Other row untouched.
        assertEquals(50.0, result.first { it.company.symbol == "AMD" }.quote!!.price, 0.0)
    }

    @Test
    fun `ignores ticks for unknown or null-quote rows`() {
        val items = listOf(
            item("NVDA", 100.0),
            AiCompanyQuote(company = AiCompanies.bySymbol("AMD")!!, quote = null),
        )

        val unknown = applyTick(items, PriceTick("TSLA", 200.0, 0L))
        assertEquals(items, unknown)

        val nullQuote = applyTick(items, PriceTick("AMD", 200.0, 0L))
        assertEquals(null, nullQuote.first { it.company.symbol == "AMD" }.quote)
    }
}
