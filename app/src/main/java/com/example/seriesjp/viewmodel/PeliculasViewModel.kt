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
                if (query.isBlank()) {
                    // Si la búsqueda está vacía, recarga lista completa
                    loadPeliculas()
                } else {
                    val response = RetrofitInstance.api.searchPeliculas(apiKey, query)
                    if (response.isSuccessful) {
                        _peliculasList.value = response.body()?.results ?: emptyList()
                    } else {
                        Log.e("PeliculasViewModel", "Error búsqueda: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("PeliculasViewModel", "Exception búsqueda: $e")
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

    fun cargarRecomendaciones(peliculaId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getMovieRecommendations(peliculaId, apiKey)
                if (response.isSuccessful) {
                    _recommendedPeliculas.value = response.body()?.results ?: emptyList()
                } else {
                    Log.e("PeliculasViewModel", "Error recomendación: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PeliculasViewModel", "Exception recomendación: $e")
            }
        }
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

    // --- Gestión de Mi Lista en Firestore ---

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

    // Mapa local peliculaId -> puntuacion (1..10, por ejemplo)
    private val _ratings = mutableStateOf<Map<Int, Int>>(emptyMap())
    val ratings: State<Map<Int, Int>> = _ratings

    /**
     * Carga puntuaciones desde Firestore: el documento userId contiene un Map<String, Int>
     * con claves como "123" que se convierten a Int aquí.
     */
    fun cargarPuntuacionesDesdeFirestore(userId: String) {
        firestore.collection("puntuacionesPeliculas")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val data = doc.data ?: emptyMap<String, Any>()
                // data: Map<String, Any>, donde la clave es "peliculaId" como String
                val map = data.mapNotNull { entry ->
                    val keyInt = entry.key.toIntOrNull()
                    val valueInt = when (val v = entry.value) {
                        is Long -> v.toInt()
                        is Double -> v.toInt()
                        is Number -> v.toInt()
                        else -> null
                    }
                    if (keyInt != null && valueInt != null) {
                        keyInt to valueInt
                    } else null
                }.toMap()
                _ratings.value = map
            }
            .addOnFailureListener {
                Log.e("PeliculasVM", "Error cargar puntuaciones: ${it.message}")
            }
    }

    /**
     * Guarda la puntuación en local y en Firestore, convirtiendo claves Int a String.
     */
    fun guardarPuntuacion(userId: String, peliculaId: Int, puntuacion: Int) {
        // Actualizar primero el estado local
        val nuevaMapaLocal = _ratings.value.toMutableMap()
        nuevaMapaLocal[peliculaId] = puntuacion
        _ratings.value = nuevaMapaLocal

        // Convertir el Map<Int, Int> a Map<String, Int> antes de enviar a Firestore
        val mapaStringKey: Map<String, Int> = nuevaMapaLocal.mapKeys { it.key.toString() }
        firestore.collection("puntuacionesPeliculas")
            .document(userId)
            .set(mapaStringKey)
            .addOnSuccessListener {
                Log.d("PeliculasVM", "Puntuación guardada en Firestore: $mapaStringKey")
            }
            .addOnFailureListener {
                Log.e("PeliculasVM", "Error guardar puntuación: ${it.message}")
            }
    }

    // --- Gestión de comentarios ---

    private val _comentarios = mutableStateOf<Map<Int, List<Comentario>>>(emptyMap())
    val comentarios: State<Map<Int, List<Comentario>>> = _comentarios

    /**
     * Carga comentarios de una película. Cada documento id = peliculaId (String) con campo "comentarios": List<Comentario>
     */
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

    /**
     * Agrega un comentario localmente y lo guarda en Firestore.
     */
    fun agregarComentario(peliculaId: Int, comentario: Comentario) {
        // Actualizar estado local
        val listaActual = _comentarios.value[peliculaId]?.toMutableList() ?: mutableListOf()
        listaActual.add(comentario)
        val mapaActual = _comentarios.value.toMutableMap()
        mapaActual[peliculaId] = listaActual
        _comentarios.value = mapaActual

        // Guardar en Firestore: el campo "comentarios" es una lista de objetos Comentario
        val datos = hashMapOf("comentarios" to listaActual)
        firestore.collection("comentariosPeliculas")
            .document(peliculaId.toString())
            .set(datos)
            .addOnSuccessListener {
                Log.d("PeliculasVM", "Comentario guardado en Firestore para película $peliculaId")
            }
            .addOnFailureListener {
                Log.e("PeliculasVM", "Error guardar comentario: ${it.message}")
            }
    }

    // Clase auxiliar para mapear Firestore
    data class ComentariosFirestore(
        val comentarios: List<Comentario> = emptyList()
    )
}
