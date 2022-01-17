package com.example.moviesapp.model

import com.google.gson.annotations.SerializedName

data class ProductionCountries(
    @SerializedName("iso_3166_1")
    val iso: String,
    val name: String
)