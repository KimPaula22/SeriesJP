package com.example.seriesjp.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seriesjp.model.Series
import com.example.seriesjp.network.RetrofitInstance
import kotlinx.coroutines.launch

class SeriesViewModel : ViewModel() {

    // Lista de series populares obtenida desde la API
    private val _seriesList = mutableStateOf<List<Series>>(emptyList())
    val seriesList: State<List<Series>> = _seriesList

    // Lista para "Mi Lista" de series (usamos mutableStateListOf para actualizaciones automáticas)
    private val _miListaSeries = mutableStateListOf<Series>()
    val miListaSeries: List<Series> get() = _miListaSeries

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
                    Log.e("SeriesViewModel", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("SeriesViewModel", "Exception: $e")
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
                    Log.e("SeriesViewModel", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("SeriesViewModel", "Exception: $e")
            }
        }
    }

    fun agregarSerieAMiLista(serie: Series) {
        if (!_miListaSeries.contains(serie)) {
            _miListaSeries.add(serie)
        }
    }

    fun quitarSerieDeMiLista(serie: Series) {
        _miListaSeries.remove(serie)
    }

    fun refreshSeries() {
        // Actualizamos loadSeries()
        loadSeries()
    }
}
