package com.example.seriesjp.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seriesjp.model.Series
import com.example.seriesjp.model.PopularSeriesResponse
import com.example.seriesjp.network.RetrofitInstance
import kotlinx.coroutines.launch

class SeriesViewModel : ViewModel() {

    // Estado mutable para almacenar la lista de series
    var seriesList by mutableStateOf<List<Series>>(emptyList())
        private set

    // API Key de TMDb (¡Debes reemplazar con tu clave real!)
    private val apiKey = "tu_api_key_aqui"

    init {
        loadSeries()
    }

    private fun loadSeries() {
        viewModelScope.launch {
            try {
                // Llamada a la API
                val response = RetrofitInstance.api.getPopularSeries(apiKey)
                if (response.isSuccessful) {
                    val series = response.body()?.results ?: emptyList()
                    Log.d("SeriesViewModel", "Series cargadas correctamente: ${series.size} series")
                    seriesList = series
                } else {
                    Log.e("SeriesViewModel", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("SeriesViewModel", "Exception: $e")
            }
        }
    }
}