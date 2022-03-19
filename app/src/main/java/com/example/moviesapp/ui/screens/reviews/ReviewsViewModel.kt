package com.example.moviesapp.ui.screens.reviews

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.moviesapp.BaseViewModel
import com.example.moviesapp.model.MediaType
import com.example.moviesapp.repository.movie.MovieRepository
import com.example.moviesapp.repository.tv.TvSeriesRepository
import com.example.moviesapp.ui.screens.destinations.ReviewsScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ReviewsViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val tvSeriesRepository: TvSeriesRepository,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val navArgs: ReviewsScreenNavArgs = ReviewsScreenDestination.argsFrom(savedStateHandle)

    val uiState: StateFlow<ReviewsScreenUiState> = MutableStateFlow(
        ReviewsScreenUiState(
            startRoute = navArgs.startRoute,
            reviews = when (navArgs.type) {
                MediaType.Movie -> movieRepository.movieReviews(navArgs.mediaId)
                MediaType.Tv -> tvSeriesRepository.tvSeriesReviews(navArgs.mediaId)
                else -> emptyFlow()
            }.cachedIn(viewModelScope)
        )
    ).stateIn(viewModelScope, SharingStarted.Eagerly, ReviewsScreenUiState.default)
}