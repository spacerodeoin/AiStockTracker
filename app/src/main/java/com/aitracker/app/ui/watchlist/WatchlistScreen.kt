package com.aitracker.app.ui.watchlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aitracker.app.ui.AppViewModelProvider
import com.aitracker.app.ui.components.CompanyRow
import com.aitracker.app.ui.components.LoadingState
import com.aitracker.app.ui.components.MessageState

@Composable
fun WatchlistScreen(
    onCompanyClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WatchlistViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        state.isLoading -> LoadingState(modifier)
        state.items.isEmpty() -> MessageState(
            "Your watchlist is empty.\nTap the star on any company to track it here.",
            modifier,
        )
        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.items, key = { it.company.symbol }) { item ->
                CompanyRow(
                    item = item,
                    onClick = { onCompanyClick(item.company.symbol) },
                    onToggleWatch = { viewModel.toggleWatch(item.company.symbol) },
                )
            }
        }
    }
}
