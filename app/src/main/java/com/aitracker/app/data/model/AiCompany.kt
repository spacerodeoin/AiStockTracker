package com.aitracker.app.data.model

/** Broad role a company plays in the AI value chain. */
enum class AiCategory(val label: String) {
    HARDWARE("Hardware"),
    SEMICONDUCTOR("Semiconductors"),
    CLOUD("Cloud / Infra"),
    MODELS("AI Models"),
    SOFTWARE("Software"),
}

/** Static, curated metadata about an AI-related company. */
data class AiCompany(
    val symbol: String,
    val name: String,
    val category: AiCategory,
    /** What this company contributes specifically to AI hardware / compute. */
    val hardwareFocus: String,
    val description: String,
)

/** A company joined with its latest market quote (quote may be null while loading or on error). */
data class AiCompanyQuote(
    val company: AiCompany,
    val quote: StockQuote?,
    val isWatched: Boolean = false,
)
