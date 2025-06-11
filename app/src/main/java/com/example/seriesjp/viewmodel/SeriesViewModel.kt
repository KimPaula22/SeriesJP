package com.example.seriesjp.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.seriesjp.datastore.RatingPreferences
import com.example.seriesjp.model.Comentario
import com.example.seriesjp.model.Provider
import com.example.seriesjp.model.Series
import com.example.seriesjp.network.RetrofitInstance
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class SeriesViewModel(application: Application) : AndroidViewModel(application) {

    private val ratingPreferences = RatingPreferences(application)

    private val _seriesList = mutableStateOf<List<Series>>(emptyList())
    val seriesList: State<List<Series>> = _seriesList

    private val _miListaSeries = mutableStateListOf<Series>()
    val miListaSeries: List<Series> get() = _miListaSeries

    private val _recommendedSeries = mutableStateOf<List<Series>>(emptyList())
    val recommendedSeries: State<List<Series>> = _recommendedSeries

    private val _watchProviders = mutableStateOf<List<Provider>>(emptyList())
    val watchProviders: State<List<Provider>> = _watchProviders

    private val _ratings = mutableStateMapOf<Int, Int>() // Nuevo: puntuaciones
    val ratings: Map<Int, Int> get() = _ratings

    private val apiKey = "87ba94f350ec59be982686b11c25da34"

    init {
        loadSeries()
    }

    private fun loadSeries() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getPopularSeries(apiKey)
                if (response.isSuccessful) {
                    _seriesList.value = response.body()?.results ?: emptyList()
                } else {
                    Log.e("SeriesVM", "Error loadSeries: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("SeriesVM", "Exception loadSeries: $e")
            }
        }
    }

    fun searchSeries(query: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.searchSeries(apiKey, query)
                if (response.isSuccessful) {
                    _seriesList.value = response.body()?.results ?: emptyList()
                } else {
                    Log.e("SeriesVM", "Error search: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("SeriesVM", "Exception search: $e")
            }
        }
    }

    fun agregarSerieAMiLista(serie: Series) {
        if (!_miListaSeries.contains(serie)) _miListaSeries.add(serie)
    }

    fun quitarSerieDeMiLista(serie: Series) {
        _miListaSeries.remove(serie)
    }

    fun cargarRecomendaciones(tvId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getSeriesRecommendations(tvId, apiKey)
                if (response.isSuccessful) {
                    _recommendedSeries.value = response.body()?.results ?: emptyList()
                } else {
                    Log.e("SeriesVM", "Error recomend: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("SeriesVM", "Exception recomend: $e")
            }
        }
    }

    fun loadWatchProviders(tvId: Int) {
        viewModelScope.launch {
            try {
                val resp = RetrofitInstance.api.getSeriesWatchProviders(tvId, apiKey)
                if (resp.isSuccessful) {
                    val providers = resp.body()
                        ?.results
                        ?.get("ES")
                        ?.flatrate
                        ?: emptyList()
                    _watchProviders.value = providers
                } else {
                    Log.e("SeriesVM", "Error providers: ${resp.code()}")
                }
            } catch (e: Exception) {
                Log.e("SeriesVM", "Exception providers: $e")
            }
        }
    }

    fun refreshSeries() = loadSeries()

    var selectedSerie: Series? = null
        private set

    fun selectSerie(serie: Series) {
        selectedSerie = serie
    }

    // cargar puntuación desde almacenamiento
    fun cargarPuntuacion(seriesId: Int) {
        viewModelScope.launch {
            ratingPreferences.getRating(seriesId).collect { rating ->
                _ratings[seriesId] = rating
            }
        }
    }

    //  guardar puntuación
    fun guardarPuntuacion(seriesId: Int, rating: Int) {
        _ratings[seriesId] = rating
        viewModelScope.launch {
            ratingPreferences.saveRating(seriesId, rating)
        }
    }

    private val _comentarios = mutableStateMapOf<Int, List<Comentario>>()
    val comentarios: Map<Int, List<Comentario>> get() = _comentarios

    fun agregarComentario(serieId: Int, comentario: Comentario) {
        val db = Firebase.firestore
        db.collection("series")
            .document(serieId.toString())
            .collection("comentarios")
            .add(comentario)
            .addOnSuccessListener {
                Log.d("Firestore", "Comentario guardado correctamente")
                cargarComentarios(serieId)  // Recargar después de guardar
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error al guardar comentario", it)
            }
    }

    fun cargarComentarios(serieId: Int) {
        val db = Firebase.firestore
        db.collection("series")
            .document(serieId.toString())
            .collection("comentarios")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.mapNotNull { it.toObject(Comentario::class.java) }
                _comentarios[serieId] = lista
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error al cargar comentarios", it)
            }
    }
}
