package com.example.seriesjp.model

import com.google.gson.annotations.SerializedName

data class Series(
    val id: Int,
    val name: String,
    val overview: String,
    @SerializedName("poster_path") val posterPath: String,  // La URL de la imagen
    @SerializedName("first_air_date") val firstAirDate: String,
    @SerializedName("vote_average") val voteAverage: Float
)