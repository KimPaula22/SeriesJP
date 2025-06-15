package com.example.seriesjp.viewmodel

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seriesjp.model.Comentario
import com.example.seriesjp.model.Favoritos
import com.example.seriesjp.model.MiListaPeliculasFirestore
import com.example.seriesjp.model.Peliculas
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

    private val _ratings = mutableStateOf<Map<Int, Int>>(emptyMap())
    val ratings: State<Map<Int, Int>> = _ratings

    fun cargarPuntuacionesDesdeFirestore(userId: String) {
        firestore.collection("puntuacionesPeliculas")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val data = doc.data ?: emptyMap<String, Any>()
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

    fun guardarPuntuacion(userId: String, peliculaId: Int, puntuacion: Int) {
        val nuevaMapaLocal = _ratings.value.toMutableMap()
        nuevaMapaLocal[peliculaId] = puntuacion
        _ratings.value = nuevaMapaLocal

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
        val mapaActual = _comentarios.value.toMutableMap()
        mapaActual[peliculaId] = listaActual
        _comentarios.value = mapaActual

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

    data class ComentariosFirestore(
        val comentarios: List<Comentario> = emptyList()
    )

    // --- Gestión de Favoritos ---

    private val _favoritos = mutableStateListOf<Favoritos>()
    val favoritos: List<Favoritos> get() = _favoritos

    fun cargarFavoritosDesdeFirestore(userId: String) {
        firestore.collection("favoritos")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val lista = doc.toObject(FavoritosFirestore::class.java)
                _favoritos.clear()
                if (lista != null) {
                    _favoritos.addAll(lista.favoritos)
                }
            }
            .addOnFailureListener {
                Log.e("PeliculasVM", "Error al cargar favoritos: ${it.message}")
            }
    }

    fun agregarFavorito(userId: String, favorito: Favoritos) {
        if (!_favoritos.any { it.id == favorito.id }) {
            _favoritos.add(favorito)
            guardarFavoritosEnFirestore(userId)
        }
    }

    fun quitarFavorito(userId: String, favoritoId: String) {
        val eliminado = _favoritos.removeAll { it.id == favoritoId }
        if (eliminado) {
            guardarFavoritosEnFirestore(userId)
        }
    }

    private fun guardarFavoritosEnFirestore(userId: String) {
        val datos = hashMapOf("favoritos" to _favoritos)
        firestore.collection("favoritos")
            .document(userId)
            .set(datos)
            .addOnSuccessListener {
                Log.d("PeliculasVM", "Favoritos guardados en Firestore")
            }
            .addOnFailureListener {
                Log.e("PeliculasVM", "Error guardar favoritos: ${it.message}")
            }
    }

    data class FavoritosFirestore(
        val favoritos: List<Favoritos> = emptyList()
    )
}
