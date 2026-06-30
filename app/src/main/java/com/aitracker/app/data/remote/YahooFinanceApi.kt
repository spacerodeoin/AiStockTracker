package com.aitracker.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Unofficial Yahoo Finance chart endpoint. No API key required.
 * Example: https://query1.finance.yahoo.com/v8/finance/chart/NVDA?range=1mo&interval=1d
 */
interface YahooFinanceApi {

    @GET("v8/finance/chart/{symbol}")
    suspend fun getChart(
        @Path("symbol") symbol: String,
        @Query("range") range: String = "1mo",
        @Query("interval") interval: String = "1d",
    ): ChartResponse

    companion object {
        const val BASE_URL = "https://query1.finance.yahoo.com/"
    }
}
