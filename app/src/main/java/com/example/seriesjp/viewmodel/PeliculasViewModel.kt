package com.example.seriesjp.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seriesjp.model.Peliculas
import com.example.seriesjp.model.PopularMoviesResponse
import kotlinx.coroutines.launch
import com.example.seriesjp.network.RetrofitInstance
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.MutableLiveData

class PeliculasViewModel : ViewModel() {
    private val _peliculasList = mutableStateOf<List<Peliculas>>(emptyList())
    val peliculasList: State<List<Peliculas>> = _peliculasList


    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val apiKey = "87ba94f350ec59be982686b11c25da34"

    init {
        loadPeliculas()
    }

    private fun loadPeliculas() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = RetrofitInstance.api.getPopularMovies(apiKey)

                if (response.isSuccessful) {
                    val peliculas = response.body()?.results ?: emptyList()
                    _peliculasList.value = peliculas
                } else {
                    _errorMessage.value = "Error al cargar las películas. Código: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar las películas: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchPeliculas(query: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.searchPeliculas(apiKey, query)
                if (response.isSuccessful) {
                    _peliculasList.value = response.body()?.results ?: emptyList()
                } else {
                    Log.e("PeliculasViewModel", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PeliculasViewModel", "Exception: $e")
            }
        }
    }


    private val _miListaPeliculas = mutableStateListOf<Peliculas>()
    val miListaPeliculas: List<Peliculas> get() = _miListaPeliculas

    fun agregarPeliculaAMiLista(pelicula: Peliculas) {
        if (!_miListaPeliculas.contains(pelicula)) {
            _miListaPeliculas.add(pelicula)
        }
    }

    fun quitarPeliculaDeMiLista(pelicula: Peliculas) {
        _miListaPeliculas.remove(pelicula)
    }

    fun refreshPeliculas() {
        loadPeliculas()
        }
    }



