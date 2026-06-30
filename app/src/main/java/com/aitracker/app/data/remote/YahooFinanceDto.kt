package com.aitracker.app.data.remote

import com.google.gson.annotations.SerializedName

/** DTOs mapping the Yahoo Finance chart endpoint JSON response. */
data class ChartResponse(
    @SerializedName("chart") val chart: Chart?,
)

data class Chart(
    @SerializedName("result") val result: List<ChartResult>?,
    @SerializedName("error") val error: ChartError?,
)

data class ChartError(
    @SerializedName("code") val code: String?,
    @SerializedName("description") val description: String?,
)

data class ChartResult(
    @SerializedName("meta") val meta: ChartMeta?,
    @SerializedName("timestamp") val timestamp: List<Long>?,
    @SerializedName("indicators") val indicators: Indicators?,
)

data class ChartMeta(
    @SerializedName("symbol") val symbol: String?,
    @SerializedName("currency") val currency: String?,
    @SerializedName("regularMarketPrice") val regularMarketPrice: Double?,
    @SerializedName("previousClose") val previousClose: Double?,
    @SerializedName("chartPreviousClose") val chartPreviousClose: Double?,
)

data class Indicators(
    @SerializedName("quote") val quote: List<QuoteIndicator>?,
)

data class QuoteIndicator(
    @SerializedName("close") val close: List<Double?>?,
)
