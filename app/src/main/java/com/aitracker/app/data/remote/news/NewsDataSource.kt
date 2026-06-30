package com.aitracker.app.data.remote.news

import com.aitracker.app.data.model.NewsArticle

/** A swappable source of news articles (Google News RSS free, or keyed providers like NewsAPI). */
interface NewsDataSource {

    /** Human-readable provider name, useful for diagnostics. */
    val providerName: String

    suspend fun search(query: String, limit: Int = 30): List<NewsArticle>
}
