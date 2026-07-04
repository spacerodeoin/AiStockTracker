package com.aitracker.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aitracker.app.data.model.AiCompanyQuote
import com.aitracker.app.ui.theme.GreenAccent
import com.aitracker.app.ui.theme.RedAccent

@Composable
fun CompanyRow(
    item: AiCompanyQuote,
    onClick: () -> Unit,
    onToggleWatch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Briefly tint the row green/red when the live price ticks up/down.
    val price = item.quote?.price
    var flashUp by remember { mutableStateOf(true) }
    var previousPrice by remember { mutableStateOf(price) }
    val flashAlpha = remember { Animatable(0f) }
    LaunchedEffect(price) {
        val prev = previousPrice
        if (price != null && prev != null && price != prev) {
            flashUp = price > prev
            flashAlpha.snapTo(0.22f)
            flashAlpha.animateTo(0f, animationSpec = tween(durationMillis = 600))
        }
        previousPrice = price
    }
    val flashColor = (if (flashUp) GreenAccent else RedAccent).copy(alpha = flashAlpha.value)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(flashColor)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.company.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = item.company.category.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = item.company.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            val quote = item.quote
            if (quote != null && quote.closeHistory.size >= 2) {
                Sparkline(
                    values = quote.closeHistory,
                    color = if (quote.isUp) GreenAccent else RedAccent,
                    modifier = Modifier
                        .width(56.dp)
                        .height(32.dp)
                        .padding(horizontal = 8.dp),
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (quote != null) {
                    Text(
                        text = formatPrice(quote.price, quote.currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    ChangeChip(text = formatPercent(quote.changePercent), isUp = quote.isUp)
                } else {
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    )
                }
            }

            IconButton(onClick = onToggleWatch) {
                Icon(
                    imageVector = if (item.isWatched) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (item.isWatched) "Remove from watchlist" else "Add to watchlist",
                    tint = if (item.isWatched) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}
