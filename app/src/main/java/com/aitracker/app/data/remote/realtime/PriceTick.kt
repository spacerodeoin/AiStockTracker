package com.aitracker.app.data.remote.realtime

/** A single real-time trade/price update for one symbol, delivered over a live stream. */
data class PriceTick(
    val symbol: String,
    val price: Double,
    val timestampMillis: Long,
)
