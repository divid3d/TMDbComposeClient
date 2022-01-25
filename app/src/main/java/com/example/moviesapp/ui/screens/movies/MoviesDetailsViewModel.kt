package com.example.moviesapp.ui.screens.movies

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.moviesapp.model.Config
import com.example.moviesapp.model.Credits
import com.example.moviesapp.model.MovieDetails
import com.example.moviesapp.model.Presentable
import com.example.moviesapp.other.appendUrls
import com.example.moviesapp.other.asFlow
import com.example.moviesapp.other.getImageUrl
import com.example.moviesapp.repository.ConfigRepository
import com.example.moviesapp.repository.FavouritesRepository
import com.example.moviesapp.repository.MovieRepository
import com.example.moviesapp.repository.RecentlyBrowsedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesDetailsViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val movieRepository: MovieRepository,
    private val favouritesRepository: FavouritesRepository,
    private val recentlyBrowsedRepository: RecentlyBrowsedRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val config: StateFlow<Config?> = configRepository.config
    private val favouritesMoviesIdsFlow: Flow<List<Int>> =
        favouritesRepository.getFavouritesMoviesIds()

    private val _movieDetails: MutableStateFlow<MovieDetails?> = MutableStateFlow(null)
    private val _credits: MutableStateFlow<Credits?> = MutableStateFlow(null)

    private val movieId: Flow<Int?> = savedStateHandle.getLiveData<Int>("movieId").asFlow()

    var similarMoviesPagingDataFlow: Flow<PagingData<Presentable>>? = null
    var moviesRecommendationPagingDataFlow: Flow<PagingData<Presentable>>? = null

    val movieDetails: StateFlow<MovieDetails?> = combine(
        _movieDetails, config, favouritesMoviesIdsFlow
    ) { movieDetails, config, favouriteMoviesIds ->
        val posterUrl = config?.getImageUrl(movieDetails?.posterPath)
        val backdropUrl = config?.getImageUrl(movieDetails?.backdropPath)

        movieDetails?.copy(
            posterUrl = posterUrl,
            backdropUrl = backdropUrl,
            isFavourite = movieDetails.id in favouriteMoviesIds
        )
    }
        .onEach { movieDetails ->
            movieDetails?.let { details ->
                recentlyBrowsedRepository.addRecentlyBrowsedMovie(details)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(10), null)

    val credits: StateFlow<Credits?> = combine(
        _credits, config
    ) { credits, config ->
        val cast = credits?.cast?.map { member ->
            val profileUrl = config?.getImageUrl(member.profilePath, size = "w185")

            member.copy(profileUrl = profileUrl)
        }

        val crew = credits?.crew?.map { member ->
            val profileUrl = config?.getImageUrl(member.profilePath, size = "w185")

            member.copy(profileUrl = profileUrl)
        }

        credits?.copy(
            cast = cast,
            crew = crew
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(10), null)

    init {
        viewModelScope.launch {
            movieId.collectLatest { movieId ->
                movieId?.let { id ->
                    similarMoviesPagingDataFlow = movieRepository.similarMovies(id)
                        .cachedIn(viewModelScope)
                        .combine(config) { moviePagingData, config ->
                            moviePagingData.map { movie ->
                                movie.appendUrls(config)
                            }
                        }

                    moviesRecommendationPagingDataFlow =
                        movieRepository.moviesRecommendations(movieId)
                            .cachedIn(viewModelScope)
                            .combine(config) { moviePagingData, config ->
                                moviePagingData.map { movie ->
                                    movie.appendUrls(config)
                                }
                            }

                    getMovieInfo(id)
                }
            }
        }
    }

    fun onLikeClick(movieDetails: MovieDetails) {
        favouritesRepository.likeMovie(movieDetails)
    }

    fun onUnlikeClick(movieDetails: MovieDetails) {
        favouritesRepository.unlikeMovie(movieDetails)
    }

    private fun getMovieInfo(movieId: Int) {
        getMovieDetails(movieId)
        getMovieCredits(movieId)
    }

    private fun getMovieDetails(movieId: Int) {
        viewModelScope.launch {
            val movieDetails = movieRepository.movieDetails(movieId)
            _movieDetails.emit(movieDetails)
        }
    }

    private fun getMovieCredits(movieId: Int) {
        viewModelScope.launch {
            val credits = movieRepository.movieCredits(movieId)
            _credits.emit(credits)
        }
    }

}