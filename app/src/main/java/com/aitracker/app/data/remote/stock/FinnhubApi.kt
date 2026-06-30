package com.aitracker.app.data.remote.stock

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Finnhub real-time quote endpoint (requires a free API key).
 * https://finnhub.io/api/v1/quote?symbol=NVDA&token=KEY
 */
interface FinnhubApi {

    @GET("api/v1/quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
        @Query("token") token: String,
    ): FinnhubQuoteDto

    companion object {
        const val BASE_URL = "https://finnhub.io/"
    }
}

/** Finnhub quote payload — `c` = current price, `pc` = previous close. */
data class FinnhubQuoteDto(
    @SerializedName("c") val current: Double?,
    @SerializedName("pc") val previousClose: Double?,
)
