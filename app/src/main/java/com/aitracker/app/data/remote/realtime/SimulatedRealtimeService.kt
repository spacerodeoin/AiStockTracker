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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * Network-free fallback feed that jitters the last-known price of each subscribed symbol on a
 * fixed cadence, so the live ticker is demonstrable without a key or when the market is closed.
 *
 * The random walk is clamped to a band around each primed base price so prices stay realistic.
 * [scope], [clock] and [random] are injectable to keep the service deterministic under test.
 */
class SimulatedRealtimeService(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob()),
    private val tickIntervalMillis: Long = DEFAULT_TICK_INTERVAL_MILLIS,
    private val maxStepFraction: Double = DEFAULT_MAX_STEP_FRACTION,
    private val bandFraction: Double = DEFAULT_BAND_FRACTION,
    private val random: Random = Random.Default,
    private val clock: () -> Long = System::currentTimeMillis,
) : RealtimeQuoteService {

    override val providerName: String = "Simulated feed"

    private val _connectionState = MutableStateFlow(ConnectionState.Connecting)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _ticks = MutableSharedFlow<PriceTick>(extraBufferCapacity = 256)
    override val ticks: SharedFlow<PriceTick> = _ticks.asSharedFlow()

    private val basePrices = ConcurrentHashMap<String, Double>()
    private val lastPrices = ConcurrentHashMap<String, Double>()
    private val subscribed = ConcurrentHashMap.newKeySet<String>()

    private var loopJob: Job? = null

    override fun prime(basePrices: Map<String, Double>) {
        basePrices.forEach { (symbol, price) ->
            if (price > 0.0) {
                this.basePrices[symbol] = price
                this.lastPrices.putIfAbsent(symbol, price)
            }
        }
    }

    override fun connect() {
        _connectionState.value = ConnectionState.LiveDemo
        if (loopJob?.isActive == true) return
        loopJob = scope.launch {
            while (isActive) {
                delay(tickIntervalMillis)
                emitRound()
            }
        }
    }

    private fun emitRound() {
        for (symbol in subscribed) {
            val base = basePrices[symbol] ?: continue
            val last = lastPrices[symbol] ?: base
            val step = (random.nextDouble() * 2.0 - 1.0) * maxStepFraction
            val next = (last * (1.0 + step))
                .coerceIn(base * (1.0 - bandFraction), base * (1.0 + bandFraction))
            lastPrices[symbol] = next
            _ticks.tryEmit(PriceTick(symbol = symbol, price = next, timestampMillis = clock()))
        }
    }

    override fun subscribe(symbols: Collection<String>) {
        subscribed.addAll(symbols)
    }

    override fun unsubscribe(symbols: Collection<String>) {
        subscribed.removeAll(symbols.toSet())
    }

    override fun disconnect() {
        loopJob?.cancel()
        loopJob = null
        scope.cancel()
    }

    companion object {
        const val DEFAULT_TICK_INTERVAL_MILLIS = 30_000L
        /** Max per-tick move as a fraction of the last price (±0.4%). */
        const val DEFAULT_MAX_STEP_FRACTION = 0.004
        /** Hard band around the base price the walk may never leave (±8%). */
        const val DEFAULT_BAND_FRACTION = 0.08
    }
}
