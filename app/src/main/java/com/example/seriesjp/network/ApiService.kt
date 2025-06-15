package com.example.seriesjp.network

import com.example.seriesjp.model.Peliculas
import com.example.seriesjp.model.PopularMoviesResponse
import com.example.seriesjp.model.PopularSeriesResponse
import com.example.seriesjp.model.Series
import com.example.seriesjp.model.WatchProvidersResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
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



    // Recomendaciones de series para una serie dada
    @GET("tv/{seriesId}/recommendations")
    suspend fun getSeriesRecommendations(
        @Path("seriesId") seriesId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES"
    ): Response<PopularSeriesResponse>

    // Recomendaciones de películas para una película dada
    @GET("movie/{movieId}/recommendations")
    suspend fun getMovieRecommendations(
        @Path("movieId") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES"
    ): Response<PopularMoviesResponse>

    // Para saber los proveedores de pelis y series (amazon, netflix ...)

    @GET("movie/{movie_id}/watch/providers")
    suspend fun getMovieWatchProviders(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Response<WatchProvidersResponse>

    @GET("tv/{tv_id}/watch/providers")
    suspend fun getSeriesWatchProviders(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String
    ): Response<WatchProvidersResponse>



    @GET("tv/{id}")
    suspend fun getSeriePorId(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES"
    ): Response<Series>

}
