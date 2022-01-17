package com.example.moviesapp.ui.screens.tv

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.moviesapp.data.TvSeriesDetailsResponseDataSource
import com.example.moviesapp.model.*
import com.example.moviesapp.other.asFlow
import com.example.moviesapp.other.getImageUrl
import com.example.moviesapp.repository.ConfigRepository
import com.example.moviesapp.repository.FavouritesRepository
import com.example.moviesapp.repository.TvSeriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvSeriesDetailsViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val tvSeriesRepository: TvSeriesRepository,
    private val favouritesRepository: FavouritesRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val config: StateFlow<Config?> = configRepository.config
    private val favouritesMoviesIdsFlow: Flow<List<Int>> =
        favouritesRepository.getFavouritesMoviesIds()

    private val _tvSeriesDetails: MutableStateFlow<MovieDetails?> = MutableStateFlow(null)
    private val _credits: MutableStateFlow<Credits?> = MutableStateFlow(null)

    private val tvSeriesId: Flow<Int?> = savedStateHandle.getLiveData<Int>("tvSeriesId").asFlow()

    var similarMoviesPagingDataFlow: Flow<PagingData<Presentable>>? = null
    var moviesRecommendationPagingDataFlow: Flow<PagingData<Presentable>>? = null

    val movieDetails: StateFlow<MovieDetails?> = combine(
        _tvSeriesDetails, config, favouritesMoviesIdsFlow
    ) { movieDetails, config, favouriteMoviesIds ->
        val posterUrl = config?.getImageUrl(movieDetails?.posterPath)
        val backdropUrl = config?.getImageUrl(movieDetails?.backdropPath)

        movieDetails?.copy(
            posterUrl = posterUrl,
            backdropUrl = backdropUrl,
            isFavourite = movieDetails.id in favouriteMoviesIds
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(10), null)

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
            tvSeriesId.collectLatest { tvSeriesId ->
                tvSeriesId?.let { id ->
                    val similarMoviesLoader: suspend (Int, Int, String) -> TvSeriesResponse =
                        tvSeriesRepository::similarTvSeries

                    val moviesRecommendationLoader: suspend (Int, Int, String) -> TvSeriesResponse =
                        tvSeriesRepository::tvSeriesRecommendations

                    similarMoviesPagingDataFlow = Pager(PagingConfig(pageSize = 20)) {
                        TvSeriesDetailsResponseDataSource(
                            movieId = id,
                            apiHelperMethod = similarMoviesLoader
                        )
                    }.flow.combine(config) { moviePagingData, config ->
                        moviePagingData.map { movie ->
                            movie.appendUrls(config)
                        }
                    }

                    moviesRecommendationPagingDataFlow = Pager(PagingConfig(pageSize = 20)) {
                        TvSeriesDetailsResponseDataSource(
                            movieId = id,
                            apiHelperMethod = moviesRecommendationLoader
                        )
                    }.flow.combine(config) { moviePagingData, config ->
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

    private fun getMovieInfo(tvSeriesId: Int) {
        getTvSeriesDetails(tvSeriesId)
    }

    private fun getTvSeriesDetails(tvSeriesId: Int) {
//        viewModelScope.launch {
//            val movieDetails = tvSeriesRepository.movieDetails(tvSeriesId)
//            _tvSeriesDetails.emit(movieDetails)
//        }
    }


    private fun TvSeries.appendUrls(
        config: Config?
    ): TvSeries {
        val moviePosterUrl = config?.getImageUrl(posterPath)
        val movieBackdropUrl = config?.getImageUrl(backdropPath, size = "w300")

        return copy(
            posterUrl = moviePosterUrl,
            backdropUrl = movieBackdropUrl
        )
    }

}