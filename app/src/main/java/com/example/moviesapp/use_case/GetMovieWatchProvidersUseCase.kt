package com.example.moviesapp.use_case

import com.example.moviesapp.api.ApiResponse
import com.example.moviesapp.api.awaitApiResponse
import com.example.moviesapp.model.DeviceLanguage
import com.example.moviesapp.model.WatchProviders
import com.example.moviesapp.repository.movie.MovieRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetMovieWatchProvidersUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    suspend operator fun invoke(
        movieId: Int,
        deviceLanguage: DeviceLanguage
    ): ApiResponse<WatchProviders?> {
        val response = movieRepository.watchProviders(movieId).awaitApiResponse()

        return when (response) {
            is ApiResponse.Success -> {
                val results = response.data?.results
                val watchProviders = results?.getOrElse(deviceLanguage.region) { null }
                ApiResponse.Success(watchProviders)
            }
            is ApiResponse.Failure -> ApiResponse.Failure(response.apiError)
            is ApiResponse.Exception -> ApiResponse.Exception(response.exception)
        }
    }
}