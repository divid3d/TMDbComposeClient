package com.example.moviesapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class TvSeriesFavourite(
    @PrimaryKey
    override val id: Int,

    @ColumnInfo(name = "backdrop_path")
    override val backdropPath: String?,

    @ColumnInfo(name = "poster_path")
    override val posterPath: String?,

    val name: String,

    override val overview: String,

    @ColumnInfo(name = "vote_average")
    override val voteAverage: Float,

    @ColumnInfo(name = "vote_count")
    override val voteCount: Int,

    @Ignore
    override val backdropUrl: String?,

    @Ignore
    override val posterUrl: String?
) : Presentable {
    constructor(
        id: Int,
        backdropPath: String?,
        posterPath: String?,
        name: String,
        overview: String,
        voteAverage: Float,
        voteCount: Int
    ) : this(
        id,
        backdropPath,
        posterPath,
        name,
        overview,
        voteAverage,
        voteCount,
        null,
        null
    )

    override val title: String
        get() = name
}