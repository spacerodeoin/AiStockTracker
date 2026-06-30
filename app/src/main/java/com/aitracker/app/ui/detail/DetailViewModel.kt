package com.aitracker.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aitracker.app.data.model.AiCompanies
import com.aitracker.app.data.model.AiCompany
import com.aitracker.app.data.model.NewsArticle
import com.aitracker.app.data.model.StockQuote
import com.aitracker.app.data.repository.NewsRepository
import com.aitracker.app.data.repository.StockRepository
import com.aitracker.app.data.repository.WatchlistRepository
import com.aitracker.app.ui.navigation.Routes
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val company: AiCompany? = null,
    val quote: StockQuote? = null,
    val news: List<NewsArticle> = emptyList(),
    val isWatched: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val stockRepository: StockRepository,
    private val newsRepository: NewsRepository,
    private val watchlistRepository: WatchlistRepository,
) : ViewModel() {

    private val symbol: String = savedStateHandle.get<String>(Routes.ARG_SYMBOL).orEmpty()

    private val _uiState = MutableStateFlow(DetailUiState(company = AiCompanies.bySymbol(symbol)))
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        observeWatchlist()
        load()
    }

    private fun observeWatchlist() {
        viewModelScope.launch {
            watchlistRepository.watchedSymbols.collect { symbols ->
                _uiState.update { it.copy(isWatched = symbol in symbols) }
            }
        }
    }

    fun load() {
        val company = AiCompanies.bySymbol(symbol)
        if (company == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Unknown company") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, company = company) }
            val quoteDeferred = async { runCatching { stockRepository.getQuote(symbol) }.getOrNull() }
            val newsDeferred = async { runCatching { newsRepository.getCompanyNews(company.name) }.getOrDefault(emptyList()) }
            val quote = quoteDeferred.await()
            val news = newsDeferred.await()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    quote = quote,
                    news = news,
                    errorMessage = if (quote == null) "Live price unavailable" else null,
                )
            }
        }
    }

    fun toggleWatch() {
        viewModelScope.launch { watchlistRepository.toggle(symbol) }
    }
}
