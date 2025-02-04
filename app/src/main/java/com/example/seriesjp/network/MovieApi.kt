package com.example.seriesjp.network

import com.example.seriesjp.model.Pelicula
import com.example.seriesjp.model.Series
import retrofit2.http.GET
import retrofit2.http.Query

interface MovieApi {

    // Obtén una lista de películas populares
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES"
    ): List<Pelicula>

    // Obtén una lista de series populares
    @GET("tv/popular")
    suspend fun getPopularSeries(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES"
    ): List<Series>
}