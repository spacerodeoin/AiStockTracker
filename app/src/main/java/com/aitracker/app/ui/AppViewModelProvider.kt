package com.aitracker.app.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aitracker.app.AiTrackerApp
import com.aitracker.app.ui.detail.DetailViewModel
import com.aitracker.app.ui.news.NewsViewModel
import com.aitracker.app.ui.stocks.StocksViewModel
import com.aitracker.app.ui.watchlist.WatchlistViewModel

/** Central factory that builds every ViewModel from the app's [AppContainer]. */
object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer {
            StocksViewModel(
                stockRepository = app().container.stockRepository,
                watchlistRepository = app().container.watchlistRepository,
                realtimeQuoteService = app().container.realtimeQuoteService,
            )
        }
        initializer {
            NewsViewModel(newsRepository = app().container.newsRepository)
        }
        initializer {
            WatchlistViewModel(
                stockRepository = app().container.stockRepository,
                watchlistRepository = app().container.watchlistRepository,
            )
        }
        initializer {
            DetailViewModel(
                savedStateHandle = createSavedStateHandle(),
                stockRepository = app().container.stockRepository,
                newsRepository = app().container.newsRepository,
                watchlistRepository = app().container.watchlistRepository,
            )
        }
    }
}

private fun CreationExtras.app(): AiTrackerApp = this[APPLICATION_KEY] as AiTrackerApp
