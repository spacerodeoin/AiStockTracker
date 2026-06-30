package com.aitracker.app.ui.stocks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aitracker.app.data.model.AiCategory
import com.aitracker.app.ui.AppViewModelProvider
import com.aitracker.app.ui.components.CompanyRow
import com.aitracker.app.ui.components.LoadingState
import com.aitracker.app.ui.components.MessageState
import com.aitracker.app.ui.components.ProviderFooter

@Composable
fun StocksScreen(
    onCompanyClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StocksViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        state.isLoading -> LoadingState(modifier)
        state.items.isEmpty() && state.errorMessage != null ->
            MessageState(state.errorMessage!!, modifier)
        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                CategoryFilterRow(
                    selected = state.selectedCategory,
                    onSelect = viewModel::selectCategory,
                    onRefresh = { viewModel.load(isRefresh = true) },
                )
            }
            state.errorMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }
            items(state.visibleItems, key = { it.company.symbol }) { item ->
                CompanyRow(
                    item = item,
                    onClick = { onCompanyClick(item.company.symbol) },
                    onToggleWatch = { viewModel.toggleWatch(item.company.symbol) },
                )
            }
            if (state.providerName.isNotEmpty()) {
                item { ProviderFooter("Prices via ${state.providerName}") }
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    selected: AiCategory?,
    onSelect: (AiCategory?) -> Unit,
    onRefresh: () -> Unit,
) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(
                    selected = selected == null,
                    onClick = { onSelect(null) },
                    label = { Text("All") },
                )
            }
            items(AiCategory.entries) { category ->
                FilterChip(
                    selected = selected == category,
                    onClick = { onSelect(category) },
                    label = { Text(category.label) },
                )
            }
        }
        IconButton(onClick = onRefresh) {
            Icon(Icons.Filled.Refresh, contentDescription = "Refresh prices")
        }
    }
}
