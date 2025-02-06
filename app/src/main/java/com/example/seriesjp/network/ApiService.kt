package com.example.seriesjp.network

import com.example.seriesjp.model.Peliculas
import com.example.seriesjp.model.PopularMoviesResponse
import com.example.seriesjp.model.PopularSeriesResponse
import com.example.seriesjp.model.Series
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    // Obtener películas populares
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES"
    ): Response<PopularMoviesResponse>

    // Obtener series populares
    @GET("tv/popular")
    suspend fun getPopularSeries(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES"
    ): Response<PopularSeriesResponse>

    // Endpoint para buscar series
    @GET("search/tv")
    suspend fun searchSeries(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): Response<PopularSeriesResponse>


    // Buscar películas por nombre
    @GET("search/movie")
    suspend fun searchPeliculas(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): Response<PopularMoviesResponse>

}
