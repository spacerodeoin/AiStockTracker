package com.aitracker.app.ui.news

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aitracker.app.data.model.NewsArticle
import com.aitracker.app.ui.AppViewModelProvider
import com.aitracker.app.ui.components.LoadingState
import com.aitracker.app.ui.components.MessageState
import com.aitracker.app.ui.components.ProviderFooter

@Composable
fun NewsScreen(
    modifier: Modifier = Modifier,
    viewModel: NewsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        state.isLoading -> LoadingState(modifier)
        state.articles.isEmpty() -> MessageState(state.errorMessage ?: "No news available.", modifier)
        else -> LazyColumn(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.articles, key = { it.link.ifEmpty { it.title } }) { article ->
                NewsCard(article)
            }
            if (state.providerName.isNotEmpty()) {
                item { ProviderFooter("News via ${state.providerName}") }
            }
        }
    }
}

@Composable
fun NewsCard(article: NewsArticle, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (article.link.isNotEmpty()) {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, article.link.toUri()))
                    }
                }
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = listOf(article.source, article.publishedAt)
                    .filter { it.isNotBlank() }
                    .joinToString("  •  "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}
