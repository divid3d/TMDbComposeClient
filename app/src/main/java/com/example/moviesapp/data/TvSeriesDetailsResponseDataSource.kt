package com.example.moviesapp.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.moviesapp.model.TvSeries
import com.example.moviesapp.model.TvSeriesResponse

class TvSeriesDetailsResponseDataSource(
    private val movieId: Int,
    private inline val apiHelperMethod: suspend (Int, Int, String) -> TvSeriesResponse
) : PagingSource<Int, TvSeries>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TvSeries> {
        return try {
            val nextPage = params.key ?: 1
            val movieResponse = apiHelperMethod(movieId, nextPage, "pl-PL")

            val currentPage = movieResponse.page
            val totalPages = movieResponse.totalPages

            LoadResult.Page(
                data = movieResponse.tvSeries,
                prevKey = if (nextPage == 1) null else nextPage - 1,
                nextKey = if (currentPage + 1 > totalPages) null else currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, TvSeries>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

}