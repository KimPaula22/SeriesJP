package com.example.seriesjp.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.seriesjp.viewmodel.FavoritesViewModel
import com.example.seriesjp.model.Favoritos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    userId: String,
    favoritesViewModel: FavoritesViewModel
) {
    // Observar el StateFlow de favoritos
    val favoritesList by favoritesViewModel.favorites.collectAsState()
    var isLoading by remember { mutableStateOf(true) }

    // Cargar favoritos solo una vez
    LaunchedEffect(userId) {
        favoritesViewModel.loadFavorites(userId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favoritos") }
            )
        },
        content = { padding ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                if (favoritesList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No tienes favoritos guardados.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        items(favoritesList) { favorito ->
                            FavoriteItem(favorito)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun FavoriteItem(favorito: Favoritos) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = favorito.titulo,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
