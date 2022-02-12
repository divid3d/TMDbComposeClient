package com.example.moviesapp.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.moviesapp.model.DeviceLanguage
import com.example.moviesapp.model.TvSeries
import com.example.moviesapp.model.TvSeriesResponse
import retrofit2.HttpException
import java.io.IOException

class TvSeriesDetailsResponseDataSource(
    private val tvSeriesId: Int,
    private val language: String = DeviceLanguage.default.languageCode,
    private inline val apiHelperMethod: suspend (Int, Int, String) -> TvSeriesResponse
) : PagingSource<Int, TvSeries>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TvSeries> {
        return try {
            val nextPage = params.key ?: 1
            val movieResponse = apiHelperMethod(tvSeriesId, nextPage, language)

            val currentPage = movieResponse.page
            val totalPages = movieResponse.totalPages

            LoadResult.Page(
                data = movieResponse.tvSeries,
                prevKey = if (nextPage == 1) null else nextPage - 1,
                nextKey = if (currentPage + 1 > totalPages) null else currentPage + 1
            )
        } catch (e: IOException) {
            return LoadResult.Error(e)
        } catch (e: HttpException) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, TvSeries>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

}