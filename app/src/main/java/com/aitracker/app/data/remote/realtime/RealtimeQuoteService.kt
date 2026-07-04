package com.aitracker.app.data.remote.realtime

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A live, bidirectional source of price updates.
 *
 * The connection is inherently two-way: the client pushes [subscribe] / [unsubscribe] frames
 * **upstream** to control which symbols it wants, while [ticks] carry price updates **downstream**
 * from the server. [connectionState] reflects the current link status for the UI.
 */
interface RealtimeQuoteService {

    /** Human-readable provider name, useful for diagnostics. */
    val providerName: String

    val connectionState: StateFlow<ConnectionState>

    /** Downstream price updates (server -> client). */
    val ticks: SharedFlow<PriceTick>

    /**
     * Seed the last-known price per symbol. Used by the simulated feed to jitter from a realistic
     * base; a no-op for real WebSocket providers that carry their own prices.
     */
    fun prime(basePrices: Map<String, Double>)

    /** Open the connection (idempotent). */
    fun connect()

    /** Request live updates for [symbols] (client -> server). */
    fun subscribe(symbols: Collection<String>)

    /** Stop live updates for [symbols] (client -> server). */
    fun unsubscribe(symbols: Collection<String>)

    /** Close the connection and release resources. */
    fun disconnect()
}
