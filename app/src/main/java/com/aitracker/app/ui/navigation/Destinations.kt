package com.aitracker.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Article
import androidx.compose.ui.graphics.vector.ImageVector

/** Top-level tabs shown in the bottom navigation bar. */
enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    STOCKS("stocks", "Companies", Icons.AutoMirrored.Filled.ShowChart),
    NEWS("news", "News", Icons.Outlined.Article),
    WATCHLIST("watchlist", "Watchlist", Icons.Filled.Star),
}

object Routes {
    const val DETAIL = "detail/{symbol}"
    fun detail(symbol: String) = "detail/$symbol"
    const val ARG_SYMBOL = "symbol"
}
