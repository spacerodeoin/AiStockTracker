package com.aitracker.app.ui.components

import com.aitracker.app.data.model.StockQuote
import java.util.Locale
import kotlin.math.abs

fun formatPrice(value: Double, currency: String = "USD"): String {
    val symbol = when (currency.uppercase(Locale.US)) {
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        else -> ""
    }
    return symbol + String.format(Locale.US, "%,.2f", value) +
        if (symbol.isEmpty()) " $currency" else ""
}

fun formatChange(quote: StockQuote): String {
    val sign = if (quote.change >= 0) "+" else "-"
    val abs = abs(quote.change)
    val absPct = abs(quote.changePercent)
    return String.format(Locale.US, "%s%.2f (%s%.2f%%)", sign, abs, sign, absPct)
}

fun formatPercent(value: Double): String {
    val sign = if (value >= 0) "+" else "-"
    return String.format(Locale.US, "%s%.2f%%", sign, abs(value))
}
