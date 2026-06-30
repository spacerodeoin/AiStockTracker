package com.aitracker.app.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aitracker.app.data.model.AiCompanies
import com.aitracker.app.data.model.AiCompanyQuote
import com.aitracker.app.data.repository.StockRepository
import com.aitracker.app.data.repository.WatchlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WatchlistUiState(
    val isLoading: Boolean = true,
    val items: List<AiCompanyQuote> = emptyList(),
)

class WatchlistViewModel(
    private val stockRepository: StockRepository,
    private val watchlistRepository: WatchlistRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            watchlistRepository.watchedSymbols.collect { symbols ->
                loadFor(symbols)
            }
        }
    }

    private suspend fun loadFor(symbols: Set<String>) {
        if (symbols.isEmpty()) {
            _uiState.update { it.copy(isLoading = false, items = emptyList()) }
            return
        }
        _uiState.update { it.copy(isLoading = it.items.isEmpty()) }
        val companies = AiCompanies.all.filter { it.symbol in symbols }
        val quotes = runCatching { stockRepository.getQuotes(companies.map { it.symbol }) }
            .getOrDefault(emptyMap())
        _uiState.update {
            it.copy(
                isLoading = false,
                items = companies.map { company ->
                    AiCompanyQuote(company = company, quote = quotes[company.symbol], isWatched = true)
                },
            )
        }
    }

    fun toggleWatch(symbol: String) {
        viewModelScope.launch { watchlistRepository.toggle(symbol) }
    }
}
