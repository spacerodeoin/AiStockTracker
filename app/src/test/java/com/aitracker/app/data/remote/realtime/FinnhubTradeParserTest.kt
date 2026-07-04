package com.aitracker.app.data.remote.realtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinnhubTradeParserTest {

    @Test
    fun `parses trade frame into ticks`() {
        val json = """
            {"type":"trade","data":[
              {"s":"NVDA","p":123.45,"t":1700000000000,"v":10},
              {"s":"AMD","p":98.7,"t":1700000000500,"v":5}
            ]}
        """.trimIndent()

        val ticks = parseFinnhubTrades(json)

        assertEquals(2, ticks.size)
        assertEquals(PriceTick("NVDA", 123.45, 1700000000000), ticks[0])
        assertEquals(PriceTick("AMD", 98.7, 1700000000500), ticks[1])
    }

    @Test
    fun `ignores ping frames`() {
        assertTrue(parseFinnhubTrades("""{"type":"ping"}""").isEmpty())
    }

    @Test
    fun `ignores malformed and non-object frames`() {
        assertTrue(parseFinnhubTrades("not json").isEmpty())
        assertTrue(parseFinnhubTrades("[]").isEmpty())
        assertTrue(parseFinnhubTrades("""{"type":"trade"}""").isEmpty())
    }

    @Test
    fun `skips entries missing symbol or price`() {
        val json = """{"type":"trade","data":[{"s":"NVDA"},{"p":10.0},{"s":"AMD","p":50.0,"t":1}]}"""

        val ticks = parseFinnhubTrades(json)

        assertEquals(1, ticks.size)
        assertEquals("AMD", ticks.single().symbol)
    }
}
