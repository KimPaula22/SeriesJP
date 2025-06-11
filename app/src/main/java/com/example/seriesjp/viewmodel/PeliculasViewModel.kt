package com.example.seriesjp.viewmodel

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seriesjp.model.Comentario
import com.example.seriesjp.model.Peliculas
import com.example.seriesjp.model.MiListaPeliculasFirestore
import com.example.seriesjp.model.Provider
import com.example.seriesjp.network.RetrofitInstance
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class PeliculasViewModel : ViewModel() {
    private val _peliculasList = mutableStateOf<List<Peliculas>>(emptyList())
    val peliculasList: State<List<Peliculas>> = _peliculasList

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _recommendedPeliculas = mutableStateOf<List<Peliculas>>(emptyList())
    val recommendedPeliculas: State<List<Peliculas>> = _recommendedPeliculas

    private val apiKey = "87ba94f350ec59be982686b11c25da34"

    private val firestore = FirebaseFirestore.getInstance()

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

    fun cargarRecomendaciones(peliculaId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getMovieRecommendations(peliculaId, apiKey)
                if (response.isSuccessful) {
                    _recommendedPeliculas.value = response.body()?.results ?: emptyList()
                } else {
                    Log.e("PeliculasViewModel", "Error recomendacion: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PeliculasViewModel", "Exception recomendacion: $e")
            }
        }
    }

    fun quitarPeliculaDeMiLista(pelicula: Peliculas) {
        _miListaPeliculas.remove(pelicula)
    }

    fun refreshPeliculas() {
        loadPeliculas()
    }

    private val _watchProviders = mutableStateOf<List<Provider>>(emptyList())
    val watchProviders: State<List<Provider>> = _watchProviders

    /**
     * Carga los proveedores de la película en España ("ES")
     */
    fun loadWatchProviders(movieId: Int) {
        viewModelScope.launch {
            try {
                val resp = RetrofitInstance.api.getMovieWatchProviders(movieId, apiKey)
                if (resp.isSuccessful) {
                    val body = resp.body()
                    val providers = body?.results
                        ?.get("ES")
                        ?.flatrate
                        ?: emptyList()
                    _watchProviders.value = providers
                } else {
                    Log.e("PeliculasVM", "Error providers: ${resp.code()}")
                }
            } catch (e: Exception) {
                Log.e("PeliculasVM", "Excepción providers: $e")
            }
        }
    }

    fun cargarMiListaDesdeFirestore(userId: String) {
        firestore.collection("miListaPeliculas")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val lista = doc.toObject(MiListaPeliculasFirestore::class.java)
                _miListaPeliculas.clear()
                if (lista != null) {
                    _miListaPeliculas.addAll(lista.peliculas)
                }
            }
            .addOnFailureListener {
                Log.e("PeliculasViewModel", "Error al cargar MiLista desde Firestore: ${it.message}")
            }
    }

    // --- Gestión de puntuaciones ---

    // Mapa peliculaId -> puntuacion (1..5)
    private val _ratings = mutableStateOf<Map<Int, Int>>(emptyMap())
    val ratings: State<Map<Int, Int>> = _ratings

    fun cargarPuntuacionesDesdeFirestore(userId: String) {
        firestore.collection("puntuacionesPeliculas")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val data = doc.data ?: emptyMap<String, Long>()
                val map = data.mapNotNull {
                    val key = it.key.toIntOrNull()
                    val value = (it.value as? Long)?.toInt()
                    if (key != null && value != null) key to value else null
                }.toMap()
                _ratings.value = map
            }
            .addOnFailureListener {
                Log.e("PeliculasVM", "Error cargar puntuaciones: ${it.message}")
            }
    }

    fun guardarPuntuacion(userId: String, peliculaId: Int, puntuacion: Int) {
        val nuevaMapa = _ratings.value.toMutableMap()
        nuevaMapa[peliculaId] = puntuacion
        _ratings.value = nuevaMapa

        firestore.collection("puntuacionesPeliculas")
            .document(userId)
            .set(nuevaMapa)
            .addOnFailureListener {
                Log.e("PeliculasVM", "Error guardar puntuacion: ${it.message}")
            }
    }


    private val _comentarios = mutableStateOf<Map<Int, List<Comentario>>>(emptyMap())
    val comentarios: State<Map<Int, List<Comentario>>> = _comentarios

    fun cargarComentariosDesdeFirestore(peliculaId: Int) {
        firestore.collection("comentariosPeliculas")
            .document(peliculaId.toString())
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val lista = doc.toObject(ComentariosFirestore::class.java)?.comentarios ?: emptyList()
                    val mapaActual = _comentarios.value.toMutableMap()
                    mapaActual[peliculaId] = lista
                    _comentarios.value = mapaActual
                }
            }
            .addOnFailureListener {
                Log.e("PeliculasVM", "Error cargar comentarios: ${it.message}")
            }
    }

    fun agregarComentario(peliculaId: Int, comentario: Comentario) {
        val listaActual = _comentarios.value[peliculaId]?.toMutableList() ?: mutableListOf()
        listaActual.add(comentario)

        // Actualizar estado local
        val mapaActual = _comentarios.value.toMutableMap()
        mapaActual[peliculaId] = listaActual
        _comentarios.value = mapaActual

        // Guardar en Firestore
        val datos = hashMapOf("comentarios" to listaActual)
        firestore.collection("comentariosPeliculas")
            .document(peliculaId.toString())
            .set(datos)
            .addOnFailureListener {
                Log.e("PeliculasVM", "Error guardar comentario: ${it.message}")
            }
    }

    // Clase auxiliar para mapear Firestore
    data class ComentariosFirestore(
        val comentarios: List<Comentario> = emptyList()
    )
}
