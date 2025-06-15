package com.example.seriesjp.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.seriesjp.viewmodel.FavoritesViewModel
import com.example.seriesjp.model.Favoritos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    userId: String,
    favoritesViewModel: FavoritesViewModel,
    navController: NavHostController
) {
    val favoritesList by favoritesViewModel.favorites.collectAsState()
    var isLoading by remember { mutableStateOf(true) }

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
                            FavoriteItem(favorito = favorito, navController = navController)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun FavoriteItem(favorito: Favoritos, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                if (favorito.tipo == "serie") {
                    navController.navigate("seriesDetails/${favorito.id}")
                } else {
                    navController.navigate("peliculaDetails/${favorito.id}")
                }
            },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            AsyncImage(
                model = favorito.posterUrl,
                contentDescription = favorito.titulo,
                modifier = Modifier
                    .width(100.dp)
                    .height(150.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = favorito.titulo,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}
