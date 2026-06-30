package com.aitracker.app.data.model

/** A single news headline parsed from the Google News RSS feed. */
data class NewsArticle(
    val title: String,
    val link: String,
    val source: String,
    val publishedAt: String,
)
