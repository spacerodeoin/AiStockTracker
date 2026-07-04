package com.aitracker.app.data.remote.realtime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Prefers the real provider [primary] but transparently falls back to a [fallback] simulated feed
 * when the provider fails or stays silent (e.g. the market is closed) past [graceMillis].
 *
 * If real trades start arriving after the fallback engaged (market opens mid-session), it switches
 * back to the live feed. Control calls ([prime]/[subscribe]/[unsubscribe]) are fanned out to both
 * so whichever source is active already has the right symbols and base prices.
 */
class AutoRealtimeQuoteService(
    private val primary: RealtimeQuoteService,
    private val fallback: RealtimeQuoteService,
    private val graceMillis: Long = DEFAULT_GRACE_MILLIS,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob()),
) : RealtimeQuoteService {

    override val providerName: String = "${primary.providerName} + fallback"

    private val _connectionState = MutableStateFlow(ConnectionState.Connecting)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _ticks = MutableSharedFlow<PriceTick>(extraBufferCapacity = 256)
    override val ticks: SharedFlow<PriceTick> = _ticks.asSharedFlow()

    @Volatile private var usingFallback = false
    @Volatile private var sawRealTick = false
    private var graceJob: Job? = null

    override fun prime(basePrices: Map<String, Double>) {
        primary.prime(basePrices)
        fallback.prime(basePrices)
    }

    override fun connect() {
        _connectionState.value = ConnectionState.Connecting
        primary.connect()

        scope.launch {
            primary.ticks.collect { tick ->
                sawRealTick = true
                if (usingFallback) engageFallback(false)
                _connectionState.value = ConnectionState.LiveReal
                _ticks.tryEmit(tick)
            }
        }
        scope.launch {
            fallback.ticks.collect { tick ->
                if (usingFallback) _ticks.tryEmit(tick)
            }
        }
        scope.launch {
            primary.connectionState.collect { state ->
                if (state == ConnectionState.Offline && !usingFallback) engageFallback(true)
            }
        }
        graceJob = scope.launch {
            delay(graceMillis)
            if (!sawRealTick) engageFallback(true)
        }
    }

    private fun engageFallback(active: Boolean) {
        usingFallback = active
        if (active) {
            fallback.connect()
            _connectionState.value = ConnectionState.LiveDemo
        }
    }

    override fun subscribe(symbols: Collection<String>) {
        primary.subscribe(symbols)
        fallback.subscribe(symbols)
    }

    override fun unsubscribe(symbols: Collection<String>) {
        primary.unsubscribe(symbols)
        fallback.unsubscribe(symbols)
    }

    override fun disconnect() {
        graceJob?.cancel()
        primary.disconnect()
        fallback.disconnect()
        scope.cancel()
    }

    companion object {
        const val DEFAULT_GRACE_MILLIS = 8_000L
    }
}
