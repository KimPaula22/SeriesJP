package com.example.seriesjp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.seriesjp.datastore.FavoritesRepository
import com.example.seriesjp.model.Favoritos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel para gestionar la lista de favoritos (series y películas).
 */
class FavoritesViewModel : ViewModel() {
    private val repository = FavoritesRepository()

    // Estado interno mutable de favoritos
    private val _favorites = MutableStateFlow<List<Favoritos>>(emptyList())
    /**
     * Exposición de los favoritos como StateFlow inmutable
     */
    val favorites: StateFlow<List<Favoritos>> = _favorites

    /**
     * Carga la lista de favoritos del usuario desde Firestore.
     */
    fun loadFavorites(userId: String) {
        // Usamos viewModelScope para evitar fugas y lanzar en background
        repository.getFavorites(userId) { list ->
            _favorites.value = list
        }
    }

    /**
     * Agrega un objeto Favorito a la colección del usuario y recarga la lista.
     */
    fun addFavorite(userId: String, favorito: Favoritos) {
        repository.addFavorite(userId, favorito) { success ->
            if (success) {
                // Volvemos a cargar la lista tras agregar
                loadFavorites(userId)
            }
        }
    }

    /**
     * Opcional: Remover un favorito (si lo deseas)
     */
    fun removeFavorite(userId: String, favoritoId: String) {
        // Implementar repository.removeFavorite si lo añades
        // repository.removeFavorite(userId, favoritoId) { success ->
        //     if (success) loadFavorites(userId)
        // }
    }


    fun getFavorites(userId: String, onResult: (List<Favoritos>) -> Unit) {
        repository.getFavorites(userId) { favorites ->
            onResult(favorites)
        }
    }
}
