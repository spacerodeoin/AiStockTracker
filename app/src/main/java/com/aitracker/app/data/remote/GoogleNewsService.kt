package com.aitracker.app.data.remote

import android.util.Xml
import com.aitracker.app.data.model.NewsArticle
import com.aitracker.app.data.remote.news.NewsDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.net.URLEncoder

/**
 * Fetches AI-related headlines from the public Google News RSS feed (no API key required)
 * and parses the returned XML into [NewsArticle]s.
 */
class GoogleNewsService(private val client: OkHttpClient) : NewsDataSource {

    override val providerName: String = "Google News"

    override suspend fun search(query: String, limit: Int): List<NewsArticle> = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val url = "https://news.google.com/rss/search?q=$encoded&hl=en-US&gl=US&ceid=US:en"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("News request failed: HTTP ${response.code}")
            }
            val body = response.body?.string().orEmpty()
            parseRss(body).take(limit)
        }
    }

    private fun parseRss(xml: String): List<NewsArticle> {
        val articles = mutableListOf<NewsArticle>()
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(xml))

        var insideItem = false
        var title = ""
        var link = ""
        var pubDate = ""
        var source = ""

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            val name = parser.name
            when (event) {
                XmlPullParser.START_TAG -> when {
                    name.equals("item", ignoreCase = true) -> {
                        insideItem = true
                        title = ""; link = ""; pubDate = ""; source = ""
                    }
                    insideItem && name.equals("title", ignoreCase = true) ->
                        title = parser.nextText().trim()
                    insideItem && name.equals("link", ignoreCase = true) ->
                        link = parser.nextText().trim()
                    insideItem && name.equals("pubDate", ignoreCase = true) ->
                        pubDate = parser.nextText().trim()
                    insideItem && name.equals("source", ignoreCase = true) ->
                        source = parser.nextText().trim()
                }
                XmlPullParser.END_TAG -> if (name.equals("item", ignoreCase = true)) {
                    insideItem = false
                    if (title.isNotEmpty()) {
                        articles += NewsArticle(
                            title = cleanTitle(title),
                            link = link,
                            source = source.ifEmpty { sourceFromTitle(title) },
                            publishedAt = formatDate(pubDate),
                        )
                    }
                }
            }
            event = parser.next()
        }
        return articles
    }

    /** Google News titles often end with " - Source Name"; strip it for the headline. */
    private fun cleanTitle(raw: String): String {
        val idx = raw.lastIndexOf(" - ")
        return if (idx > 0) raw.substring(0, idx) else raw
    }

    private fun sourceFromTitle(raw: String): String {
        val idx = raw.lastIndexOf(" - ")
        return if (idx > 0 && idx < raw.length - 3) raw.substring(idx + 3) else "Google News"
    }

    /** Trim RFC-822 pubDate down to a compact "Wed, 25 Jun" style. */
    private fun formatDate(raw: String): String {
        if (raw.isBlank()) return ""
        // e.g. "Wed, 25 Jun 2025 14:30:00 GMT" -> "Wed, 25 Jun"
        val parts = raw.split(" ")
        return if (parts.size >= 4) "${parts[0]} ${parts[1]} ${parts[2]}" else raw
    }

    companion object {
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14) AiStockTracker/1.0"
    }
}
