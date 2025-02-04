package com.example.seriesjp.model

import com.google.gson.annotations.SerializedName

data class PopularMoviesResponse(
    @SerializedName("page") val page: Int,
    @SerializedName("movies") val results: List<Pelicula>?, // Lista de películas (puede ser null)
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int
)