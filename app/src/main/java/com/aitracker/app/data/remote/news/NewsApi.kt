package com.aitracker.app.data.remote.news

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * NewsAPI "everything" endpoint (requires a free API key).
 * https://newsapi.org/v2/everything?q=...&apiKey=KEY
 */
interface NewsApi {

    @GET("v2/everything")
    suspend fun everything(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String,
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 30,
    ): NewsApiResponse

    companion object {
        const val BASE_URL = "https://newsapi.org/"
    }
}

data class NewsApiResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("articles") val articles: List<NewsApiArticleDto>?,
)

data class NewsApiArticleDto(
    @SerializedName("title") val title: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("publishedAt") val publishedAt: String?,
    @SerializedName("source") val source: NewsApiSourceDto?,
)

data class NewsApiSourceDto(
    @SerializedName("name") val name: String?,
)
