package com.example.seriesjp.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seriesjp.model.Pelicula
import com.example.seriesjp.model.PopularMoviesResponse
import kotlinx.coroutines.launch
import retrofit2.Response
import com.example.seriesjp.network.RetrofitInstance
import androidx.compose.runtime.State


class PeliculasViewModel : ViewModel() {

    // La propiedad de estado mutable es privada
    private val _peliculasList = mutableStateOf<List<Pelicula>>(emptyList())

    // La propiedad pública es solo de lectura
    val peliculasList: State<List<Pelicula>> = _peliculasList

    // API Key de TMDb (debes obtener la clave desde su portal)
    private val apiKey = "tu_api_key_aqui"

    init {
        // Cargar las películas al inicializar el ViewModel
        loadPeliculas()
    }

    private fun loadPeliculas() {
        viewModelScope.launch {
            try {
                // Llamada a la API de películas
                val response = RetrofitInstance.api.getPopularMovies(apiKey)
                if (response.isSuccessful) {
                    val peliculas = response.body()?.results ?: emptyList()
                    Log.d("PeliculasViewModel", "Películas cargadas correctamente: ${peliculas.size} películas")
                    _peliculasList.value = peliculas
                } else {
                    Log.e("PeliculasViewModel", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PeliculasViewModel", "Exception: $e")
            }
        }
    }
}
