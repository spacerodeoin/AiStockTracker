package com.aitracker.app.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aitracker.app.data.model.NewsArticle
import com.aitracker.app.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewsUiState(
    val isLoading: Boolean = true,
    val articles: List<NewsArticle> = emptyList(),
    val errorMessage: String? = null,
    val providerName: String = "",
)

class NewsViewModel(private val newsRepository: NewsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(providerName = newsRepository.providerName) }
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { newsRepository.getAiNews() }
                .onSuccess { articles ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            articles = articles,
                            errorMessage = if (articles.isEmpty()) "No headlines found." else null,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Failed to load news")
                    }
                }
        }
    }
}
