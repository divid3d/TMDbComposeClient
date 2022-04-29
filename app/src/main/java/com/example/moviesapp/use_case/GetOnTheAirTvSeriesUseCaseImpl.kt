package com.example.moviesapp.use_case

import androidx.paging.PagingData
import androidx.paging.filter
import com.example.moviesapp.model.DeviceLanguage
import com.example.moviesapp.model.TvSeries
import com.example.moviesapp.repository.tv.TvSeriesRepository
import com.example.moviesapp.use_case.interfaces.GetOnTheAirTvSeriesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class GetOnTheAirTvSeriesUseCaseImpl @Inject constructor(
    private val tvSeriesRepository: TvSeriesRepository
) : GetOnTheAirTvSeriesUseCase {
    override operator fun invoke(
        deviceLanguage: DeviceLanguage,
        filtered: Boolean
    ): Flow<PagingData<TvSeries>> {
        return tvSeriesRepository.onTheAirTvSeries(deviceLanguage).mapLatest { data ->
            if (filtered) data.filterCompleteInfo() else data
        }
    }

    private fun PagingData<TvSeries>.filterCompleteInfo(): PagingData<TvSeries> {
        return filter { tvSeries ->
            tvSeries.run {
                !backdropPath.isNullOrEmpty() &&
                        !posterPath.isNullOrEmpty() &&
                        title.isNotEmpty() &&
                        overview.isNotEmpty()
            }
        }
    }
}