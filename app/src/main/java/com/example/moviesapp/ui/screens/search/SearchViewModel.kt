package com.example.moviesapp.ui.screens.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import com.example.moviesapp.model.SearchResult
import com.example.moviesapp.other.appendUrls
import com.example.moviesapp.repository.ConfigRepository
import com.example.moviesapp.repository.DeviceRepository
import com.example.moviesapp.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val deviceRepository: DeviceRepository,
    private val searchRepository: SearchRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val queryDelay = 500.milliseconds
    private val minQueryLength = 3

    private val config = configRepository.config

    val voiceSearchAvailable: StateFlow<Boolean> = deviceRepository.speechToTextAvailable
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(10), false)

    private val _query: MutableStateFlow<String?> = MutableStateFlow(null)
    val query: StateFlow<String?> = _query.asStateFlow()

    private val _searchState: MutableStateFlow<SearchState> =
        MutableStateFlow(SearchState.EmptyQuery)
    val searchState: StateFlow<SearchState> = _searchState
        .asStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(10), SearchState.EmptyQuery)

    private val _queryLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val queryLoading: StateFlow<Boolean> = _queryLoading.asStateFlow()

    private var queryJob: Job? = null

    fun onQueryChange(query: String) {
        viewModelScope.launch {
            _query.emit(query)

            queryJob?.cancel()

            when {
                query.isBlank() -> {
                    _searchState.emit(SearchState.EmptyQuery)
                }

                query.length < minQueryLength -> {
                    _searchState.emit(SearchState.InsufficientQuery)
                }

                else -> {
                    queryJob = createQueryJob(query).apply {
                        start()
                    }
                }
            }
        }
    }

    fun onQueryClear() {
        onQueryChange("")
    }

    private fun createQueryJob(query: String): Job {
        return viewModelScope.launch {
            try {
                delay(queryDelay)

                _queryLoading.emit(true)

                val response = searchRepository.multiSearch(query = query)
                    .combine(config) { moviePagingData, config ->
                        moviePagingData.map { searchResult ->
                            searchResult.appendUrls(config)
                        }
                    }

                _searchState.emit(
                    SearchState.Result(
                        query = query,
                        data = response
                    )
                )
            } catch (e: CancellationException) {

            } finally {
                withContext(NonCancellable) {
                    _queryLoading.emit(false)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        queryJob?.cancel()
    }
}


sealed class SearchState {
    object EmptyQuery : SearchState()
    object InsufficientQuery : SearchState()
    data class Result(
        val query: String,
        val data: Flow<PagingData<SearchResult>>
    ) : SearchState()
}