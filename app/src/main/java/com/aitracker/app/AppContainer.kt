package com.aitracker.app

import android.content.Context
import android.util.Log
import com.aitracker.app.data.local.WatchlistStore
import com.aitracker.app.data.remote.GoogleNewsService
import com.aitracker.app.data.remote.YahooFinanceApi
import com.aitracker.app.data.remote.news.NewsApi
import com.aitracker.app.data.remote.news.NewsApiDataSource
import com.aitracker.app.data.remote.news.NewsDataSource
import com.aitracker.app.data.remote.stock.FinnhubApi
import com.aitracker.app.data.remote.stock.FinnhubStockDataSource
import com.aitracker.app.data.remote.stock.StockDataSource
import com.aitracker.app.data.remote.stock.YahooStockDataSource
import com.aitracker.app.data.repository.NewsRepository
import com.aitracker.app.data.repository.StockRepository
import com.aitracker.app.data.repository.WatchlistRepository
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Manual dependency container — constructed once in [AiTrackerApp]. */
class AppContainer(context: Context) {

    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            // Yahoo's endpoints reject default OkHttp UA; spoof a browser-ish UA.
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AiStockTracker/1.0")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // --- Stocks: keyed Finnhub when a key is set, otherwise free Yahoo Finance ---
    private val stockDataSource: StockDataSource =
        if (BuildConfig.FINNHUB_API_KEY.isNotBlank()) {
            val finnhubApi = retrofit(FinnhubApi.BASE_URL).create(FinnhubApi::class.java)
            FinnhubStockDataSource(finnhubApi, BuildConfig.FINNHUB_API_KEY)
        } else {
            val yahooApi = retrofit(YahooFinanceApi.BASE_URL).create(YahooFinanceApi::class.java)
            YahooStockDataSource(yahooApi)
        }

    // --- News: keyed NewsAPI when a key is set, otherwise free Google News RSS ---
    private val newsDataSource: NewsDataSource =
        if (BuildConfig.NEWS_API_KEY.isNotBlank()) {
            val newsApi = retrofit(NewsApi.BASE_URL).create(NewsApi::class.java)
            NewsApiDataSource(newsApi, BuildConfig.NEWS_API_KEY)
        } else {
            GoogleNewsService(okHttpClient)
        }

    private val watchlistStore = WatchlistStore(context.applicationContext)

    val stockRepository = StockRepository(stockDataSource)
    val newsRepository = NewsRepository(newsDataSource)
    val watchlistRepository = WatchlistRepository(watchlistStore)

    init {
        if (BuildConfig.DEBUG) {
            Log.i(
                "AppContainer",
                "Providers — stocks: ${stockDataSource.providerName}, news: ${newsDataSource.providerName}",
            )
        }
    }
}
