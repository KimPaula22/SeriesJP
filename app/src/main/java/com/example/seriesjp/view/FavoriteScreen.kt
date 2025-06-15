package com.example.seriesjp.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
            // Contenedor con el degradado para el TopAppBar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF2F2F2F), Color(0xFFF4C6D7))
                        )
                    )
            ) {
                TopAppBar(
                    title = { Text("Favoritos", color = Color.White) },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = Color.Transparent // transparente para ver el degradado
                    )
                )
            }
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF4C6D7),  // rosa claro
                                Color(0xFF2F2F2F)   // gris oscuro
                            )
                        )
                    )
                    .padding(padding)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    if (favoritesList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No tienes favoritos guardados.", color = Color.White)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(favoritesList) { favorito ->
                                FavoriteItem(favorito = favorito, navController = navController)
                            }
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
                color = Color(0xFF2F2F2F),
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}
