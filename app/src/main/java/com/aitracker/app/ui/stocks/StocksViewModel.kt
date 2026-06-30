package com.aitracker.app.ui.stocks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aitracker.app.data.model.AiCategory
import com.aitracker.app.data.model.AiCompanies
import com.aitracker.app.data.model.AiCompanyQuote
import com.aitracker.app.data.repository.StockRepository
import com.aitracker.app.data.repository.WatchlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StocksUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val items: List<AiCompanyQuote> = emptyList(),
    val selectedCategory: AiCategory? = null,
    val errorMessage: String? = null,
    val providerName: String = "",
) {
    val visibleItems: List<AiCompanyQuote>
        get() = if (selectedCategory == null) items
        else items.filter { it.company.category == selectedCategory }
}

class StocksViewModel(
    private val stockRepository: StockRepository,
    private val watchlistRepository: WatchlistRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StocksUiState())
    val uiState: StateFlow<StocksUiState> = _uiState.asStateFlow()

    private var watched: Set<String> = emptySet()

    init {
        _uiState.update { it.copy(providerName = stockRepository.providerName) }
        observeWatchlist()
        load()
    }

    private fun observeWatchlist() {
        viewModelScope.launch {
            watchlistRepository.watchedSymbols.collect { symbols ->
                watched = symbols
                _uiState.update { state ->
                    state.copy(items = state.items.map { it.copy(isWatched = it.company.symbol in symbols) })
                }
            }
        }
    }

    fun load(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = !isRefresh && it.items.isEmpty(), isRefreshing = isRefresh, errorMessage = null)
            }
            val result = runCatching {
                stockRepository.getQuotes(AiCompanies.symbols, forceRefresh = isRefresh)
            }
            result.onSuccess { quotes ->
                val items = AiCompanies.all.map { company ->
                    AiCompanyQuote(
                        company = company,
                        quote = quotes[company.symbol],
                        isWatched = company.symbol in watched,
                    )
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        items = items,
                        errorMessage = if (quotes.isEmpty()) {
                            "Couldn't load live prices. Pull to retry."
                        } else {
                            null
                        },
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = error.message ?: "Failed to load data",
                    )
                }
            }
        }
    }

    fun selectCategory(category: AiCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun toggleWatch(symbol: String) {
        viewModelScope.launch { watchlistRepository.toggle(symbol) }
    }
}
