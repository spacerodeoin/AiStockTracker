package com.aitracker.app.data.remote.news

import com.aitracker.app.data.model.NewsArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Keyed news source backed by NewsAPI. */
class NewsApiDataSource(
    private val api: NewsApi,
    private val apiKey: String,
) : NewsDataSource {

    override val providerName: String = "NewsAPI"

    override suspend fun search(query: String, limit: Int): List<NewsArticle> =
        withContext(Dispatchers.IO) {
            val response = api.everything(query = query, apiKey = apiKey, pageSize = limit)
            response.articles.orEmpty()
                .filter { !it.title.isNullOrBlank() }
                .map { dto ->
                    NewsArticle(
                        title = dto.title.orEmpty(),
                        link = dto.url.orEmpty(),
                        source = dto.source?.name.orEmpty().ifEmpty { "NewsAPI" },
                        publishedAt = formatDate(dto.publishedAt.orEmpty()),
                    )
                }
        }

    /** Trim an ISO-8601 timestamp ("2025-06-30T14:30:00Z") down to the date ("2025-06-30"). */
    private fun formatDate(raw: String): String =
        raw.substringBefore("T").ifEmpty { raw }
}
