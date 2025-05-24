package com.example.seriesjp.view

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.seriesjp.viewmodel.FavoritesViewModel
import com.example.seriesjp.model.Favoritos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    userId: String,
    favoritesViewModel: FavoritesViewModel
) {
    // El estado para almacenar los favoritos
    var favoritesList by remember { mutableStateOf<List<Favoritos>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }

    // Cargar los favoritos desde el ViewModel
    LaunchedEffect(userId) {
        favoritesViewModel.getFavorites(userId) { favorites ->
            favoritesList = favorites
            isLoading.value = false
        }
    }

    // Pantalla de favoritos
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favoritos") }
            )
        },
        content = { padding ->
            if (isLoading.value) {
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
                    LazyColumn(modifier = Modifier.padding(padding)) {
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
    // Aquí renderizas cada favorito
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
                text = favorito.titulo, // Cambia "titulo" por el campo adecuado de tu modelo
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFavoritesScreen() {
    FavoritesScreen(userId = "usuario123", favoritesViewModel = FavoritesViewModel())
}