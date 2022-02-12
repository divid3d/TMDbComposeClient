package com.example.moviesapp.ui.screens.tv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.example.moviesapp.model.DeviceLanguage
import com.example.moviesapp.model.Presentable
import com.example.moviesapp.repository.DeviceRepository
import com.example.moviesapp.repository.FavouritesRepository
import com.example.moviesapp.repository.RecentlyBrowsedRepository
import com.example.moviesapp.repository.TvSeriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class TvSeriesViewModel @Inject constructor(
    private val tvSeriesRepository: TvSeriesRepository,
    private val deviceRepository: DeviceRepository,
    private val favouritesRepository: FavouritesRepository,
    private val recentlyBrowsedRepository: RecentlyBrowsedRepository
) : ViewModel() {

    private val deviceLanguage: Flow<DeviceLanguage> = deviceRepository.deviceLanguage

    val onTheAir: Flow<PagingData<Presentable>> = deviceLanguage.map { deviceLanguage ->
        tvSeriesRepository
            .onTheAirTvSeries(deviceLanguage = deviceLanguage)
            .cachedIn(viewModelScope)
    }.flattenMerge().map { pagingData ->
        pagingData
            .filter { tvSeries ->
                tvSeries.run {
                    !backdropPath.isNullOrEmpty() && !posterPath.isNullOrEmpty() && title.isNotEmpty() && overview.isNotEmpty()
                }
            }
            .map { tvSeries -> tvSeries }
    }

    val popular: Flow<PagingData<Presentable>> = deviceLanguage.map { deviceLanguage ->
        tvSeriesRepository
            .popularTvSeries(deviceLanguage = deviceLanguage)
            .cachedIn(viewModelScope)
    }.flattenMerge().map { data -> data.map { tvSeries -> tvSeries } }

    val topRated: Flow<PagingData<Presentable>> = deviceLanguage.map { deviceLanguage ->
        tvSeriesRepository
            .topRatedTvSeries(deviceLanguage = deviceLanguage)
            .cachedIn(viewModelScope)
    }.flattenMerge().map { data -> data.map { tvSeries -> tvSeries } }

    val trending: Flow<PagingData<Presentable>> = deviceLanguage.map { deviceLanguage ->
        tvSeriesRepository
            .trendingTvSeries(deviceLanguage = deviceLanguage)
            .cachedIn(viewModelScope)
    }.flattenMerge().map { data -> data.map { tvSeries -> tvSeries } }

    val airingToday: Flow<PagingData<Presentable>> = deviceLanguage.map { deviceLanguage ->
        tvSeriesRepository
            .airingTodayTvSeries(deviceLanguage = deviceLanguage)
            .cachedIn(viewModelScope)
    }.flattenMerge().map { data -> data.map { tvSeries -> tvSeries } }

    val favourites: Flow<PagingData<Presentable>> =
        favouritesRepository.favouritesTvSeries()
            .cachedIn(viewModelScope)
            .map { data -> data.map { tvSeries -> tvSeries } }

    val recentlyBrowsed: Flow<PagingData<Presentable>> =
        recentlyBrowsedRepository.recentlyBrowsedTvSeries()
            .cachedIn(viewModelScope)
            .map { data -> data.map { tvSeries -> tvSeries } }

}