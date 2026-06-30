package com.aitracker.app.data.repository

import com.aitracker.app.data.model.NewsArticle
import com.aitracker.app.data.remote.news.NewsDataSource

/** Provides AI-development news via the configured [NewsDataSource]. */
class NewsRepository(private val dataSource: NewsDataSource) {

    val providerName: String get() = dataSource.providerName

    /** General feed for the News tab. */
    suspend fun getAiNews(): List<NewsArticle> =
        dataSource.search("artificial intelligence OR \"AI chips\" OR \"AI hardware\"")

    /** Company-specific feed for the detail screen. */
    suspend fun getCompanyNews(companyName: String): List<NewsArticle> =
        dataSource.search("\"$companyName\" AI")
}
