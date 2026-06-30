package com.aitracker.app.data.model

/** Normalized market quote derived from the Yahoo Finance chart endpoint. */
data class StockQuote(
    val symbol: String,
    val price: Double,
    val previousClose: Double,
    val currency: String,
    /** Daily closing prices (oldest -> newest) used to draw the sparkline / chart. */
    val closeHistory: List<Double>,
) {
    val change: Double get() = price - previousClose
    val changePercent: Double
        get() = if (previousClose == 0.0) 0.0 else (change / previousClose) * 100.0
    val isUp: Boolean get() = change >= 0
}
