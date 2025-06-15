package com.example.seriesjp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.seriesjp.datastore.FavoritesRepository
import com.example.seriesjp.model.Favoritos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FavoritesViewModel : ViewModel() {
    private val repository = FavoritesRepository()

    private val _favorites = MutableStateFlow<List<Favoritos>>(emptyList())
    val favorites: StateFlow<List<Favoritos>> = _favorites

    // Carga favoritos del usuario, actualiza el state flow
    fun loadFavorites(userId: String) {
        repository.getFavorites(userId) { list ->
            _favorites.value = list
        }
    }

    // Agrega favorito y recarga lista si éxito
    fun addFavorite(userId: String, favorito: Favoritos) {
        repository.addFavorite(userId, favorito) { success ->
            if (success) {
                loadFavorites(userId)
            }
        }
    }

    // Por implementar
    fun removeFavorite(userId: String, favoritoId: String) {
        // repository.removeFavorite(userId, favoritoId) { success ->
        //     if (success) loadFavorites(userId)
        // }
    }
}
