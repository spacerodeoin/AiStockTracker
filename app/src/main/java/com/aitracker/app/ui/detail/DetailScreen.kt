package com.aitracker.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aitracker.app.data.model.AiCompany
import com.aitracker.app.data.model.StockQuote
import com.aitracker.app.ui.AppViewModelProvider
import com.aitracker.app.ui.components.ChangeChip
import com.aitracker.app.ui.components.LoadingState
import com.aitracker.app.ui.components.Sparkline
import com.aitracker.app.ui.components.changeColor
import com.aitracker.app.ui.components.formatChange
import com.aitracker.app.ui.components.formatPrice
import com.aitracker.app.ui.news.NewsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val company = state.company

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(company?.symbol ?: "Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleWatch) {
                        Icon(
                            imageVector = if (state.isWatched) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Toggle watchlist",
                            tint = if (state.isWatched) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading && company == null -> LoadingState(Modifier.padding(padding))
            company == null -> Text(
                text = state.errorMessage ?: "Not found",
                modifier = Modifier.padding(padding).padding(16.dp),
            )
            else -> Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                HeaderCard(company, state.quote)
                state.quote?.let { quote ->
                    if (quote.closeHistory.size >= 2) ChartCard(quote)
                }
                AboutCard(company)
                NewsSection(state.news, isLoading = state.isLoading)
            }
        }
    }
}

@Composable
private fun HeaderCard(company: AiCompany, quote: StockQuote?) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(company.name, style = MaterialTheme.typography.titleLarge)
            Text(
                text = "${company.symbol}  •  ${company.category.label}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(12.dp))
            if (quote != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatPrice(quote.price, quote.currency),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.width(12.dp))
                    ChangeChip(text = formatChange(quote), isUp = quote.isUp)
                }
            } else {
                Text(
                    text = "Live price unavailable",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun ChartCard(quote: StockQuote) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Last 30 days",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(12.dp))
            Sparkline(
                values = quote.closeHistory,
                color = changeColor(quote.isUp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                strokeWidth = 5f,
            )
        }
    }
}

@Composable
private fun AboutCard(company: AiCompany) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("About", style = MaterialTheme.typography.titleMedium)
            Text(company.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "AI / Hardware focus",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(company.hardwareFocus, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun NewsSection(news: List<com.aitracker.app.data.model.NewsArticle>, isLoading: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Recent news", style = MaterialTheme.typography.titleMedium)
        when {
            news.isNotEmpty() -> news.forEach { NewsCard(it) }
            isLoading -> Text(
                "Loading news…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            else -> Text(
                "No recent news found.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}
