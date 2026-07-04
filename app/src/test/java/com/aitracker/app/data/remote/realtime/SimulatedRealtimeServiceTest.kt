package com.aitracker.app.data.remote.realtime

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class SimulatedRealtimeServiceTest {

    @Test
    fun `emits ticks only for subscribed symbols within the price band`() = runTest {
        val base = mapOf("NVDA" to 100.0, "AMD" to 50.0)
        val service = SimulatedRealtimeService(
            scope = backgroundScope,
            tickIntervalMillis = 1_000L,
            random = Random(42),
            clock = { 0L },
        )
        val received = mutableListOf<PriceTick>()
        backgroundScope.launch { service.ticks.collect { received += it } }
        runCurrent()

        service.prime(base)
        service.subscribe(listOf("NVDA")) // AMD primed but NOT subscribed
        service.connect()

        advanceTimeBy(3_500L) // ticks at t=1000, 2000, 3000
        runCurrent()

        assertEquals(3, received.size)
        assertTrue("only subscribed symbols tick", received.all { it.symbol == "NVDA" })
        assertTrue(
            "prices stay within the ±8% band of the base",
            received.all { it.price in (100.0 * 0.92)..(100.0 * 1.08) },
        )
    }

    @Test
    fun `unsubscribe stops further ticks`() = runTest {
        val service = SimulatedRealtimeService(
            scope = backgroundScope,
            tickIntervalMillis = 1_000L,
            random = Random(7),
            clock = { 0L },
        )
        val received = mutableListOf<PriceTick>()
        backgroundScope.launch { service.ticks.collect { received += it } }
        runCurrent()

        service.prime(mapOf("NVDA" to 100.0))
        service.subscribe(listOf("NVDA"))
        service.connect()
        advanceTimeBy(1_500L)
        runCurrent()
        val afterSubscribe = received.size
        assertTrue(afterSubscribe >= 1)

        service.unsubscribe(listOf("NVDA"))
        advanceTimeBy(3_000L)
        runCurrent()

        assertEquals("no ticks after unsubscribe", afterSubscribe, received.size)
    }
}
