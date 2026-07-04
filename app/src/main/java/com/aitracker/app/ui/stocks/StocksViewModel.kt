package com.aitracker.app.ui.stocks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aitracker.app.data.model.AiCategory
import com.aitracker.app.data.model.AiCompanies
import com.aitracker.app.data.model.AiCompanyQuote
import com.aitracker.app.data.remote.realtime.ConnectionState
import com.aitracker.app.data.remote.realtime.PriceTick
import com.aitracker.app.data.remote.realtime.RealtimeQuoteService
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
    val connectionState: ConnectionState = ConnectionState.Connecting,
    val isPaused: Boolean = false,
) {
    val visibleItems: List<AiCompanyQuote>
        get() = if (selectedCategory == null) items
        else items.filter { it.company.category == selectedCategory }
}

class StocksViewModel(
    private val stockRepository: StockRepository,
    private val watchlistRepository: WatchlistRepository,
    private val realtimeQuoteService: RealtimeQuoteService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StocksUiState())
    val uiState: StateFlow<StocksUiState> = _uiState.asStateFlow()

    private var watched: Set<String> = emptySet()

    /** Symbols the client currently has a live subscription for (the upstream state). */
    private var subscribedSymbols: Set<String> = emptySet()
    private var streamStarted = false

    init {
        _uiState.update { it.copy(providerName = stockRepository.providerName) }
        observeWatchlist()
        observeRealtime()
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

    private fun observeRealtime() {
        viewModelScope.launch {
            realtimeQuoteService.connectionState.collect { state ->
                // While paused, keep showing Paused regardless of the underlying link state.
                _uiState.update { if (it.isPaused) it else it.copy(connectionState = state) }
            }
        }
        viewModelScope.launch {
            realtimeQuoteService.ticks.collect { tick ->
                _uiState.update { it.copy(items = applyTick(it.items, tick)) }
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
                startStreaming(quotes.mapValues { (_, quote) -> quote.price })
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

    /** Seed the feed with base prices, open the connection, and subscribe to the visible symbols. */
    private fun startStreaming(basePrices: Map<String, Double>) {
        realtimeQuoteService.prime(basePrices)
        if (!streamStarted) {
            realtimeQuoteService.connect()
            streamStarted = true
        }
        syncSubscriptions()
    }

    /**
     * Diff the desired (visible, when not paused) symbols against the current subscription and send
     * the minimal set of subscribe/unsubscribe frames upstream.
     */
    private fun syncSubscriptions() {
        val desired = if (_uiState.value.isPaused) emptySet() else visibleSymbols()
        val toAdd = desired - subscribedSymbols
        val toRemove = subscribedSymbols - desired
        if (toAdd.isNotEmpty()) realtimeQuoteService.subscribe(toAdd)
        if (toRemove.isNotEmpty()) realtimeQuoteService.unsubscribe(toRemove)
        subscribedSymbols = desired
    }

    private fun visibleSymbols(): Set<String> =
        _uiState.value.visibleItems.map { it.company.symbol }.toSet()

    fun selectCategory(category: AiCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
        // Category changed which symbols are on screen — re-sync the live subscription upstream.
        if (streamStarted) syncSubscriptions()
    }

    /** Toggle the live stream. Pausing sends unsubscribe frames; resuming re-subscribes. */
    fun togglePause() {
        val paused = !_uiState.value.isPaused
        _uiState.update {
            it.copy(
                isPaused = paused,
                connectionState = if (paused) {
                    ConnectionState.Paused
                } else {
                    realtimeQuoteService.connectionState.value
                },
            )
        }
        if (streamStarted) syncSubscriptions()
    }

    fun toggleWatch(symbol: String) {
        viewModelScope.launch { watchlistRepository.toggle(symbol) }
    }

    override fun onCleared() {
        super.onCleared()
        realtimeQuoteService.disconnect()
    }
}

/**
 * Pure helper: return [items] with the price of the tick's symbol replaced. `previousClose` is left
 * untouched so `changePercent` recomputes automatically; all other rows are returned unchanged.
 */
fun applyTick(items: List<AiCompanyQuote>, tick: PriceTick): List<AiCompanyQuote> =
    items.map { item ->
        val quote = item.quote
        if (item.company.symbol == tick.symbol && quote != null) {
            item.copy(quote = quote.copy(price = tick.price))
        } else {
            item
        }
    }
