// PeliculasListScreen.kt
package com.example.seriesjp.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.seriesjp.model.Peliculas
import com.example.seriesjp.viewmodel.PeliculasViewModel

@Composable
fun PeliculasListScreen(
    viewModel: PeliculasViewModel,
    navController: NavHostController
) {
    val peliculasList = viewModel.peliculasList.value
    val recommendedPeliculas = viewModel.recommendedPeliculas.value
    val isLoading = peliculasList.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF4C6D7), Color(0xFF121212))
                )
            )
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Películas Populares",
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(peliculasList) { pelicula ->
                    PeliculaItem(
                        pelicula = pelicula,
                        onClick = {
                            viewModel.cargarRecomendaciones(pelicula.id)
                            navController.navigate("peliculaDetails/${pelicula.id}")
                        }
                    )
                }

                if (recommendedPeliculas.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Recomendaciones",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(recommendedPeliculas) { recPelicula ->
                                PeliculaTrendingItem(
                                    pelicula = recPelicula,
                                    modifier = Modifier.size(UiDefaults.TrendingItemSize),
                                    onClick = {
                                        viewModel.cargarRecomendaciones(recPelicula.id)
                                        navController.navigate("peliculaDetails/${recPelicula.id}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PeliculaItem(
    pelicula: Peliculas,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${pelicula.posterPath}",
                contentDescription = pelicula.title ?: "Película sin título",
                modifier = Modifier
                    .size(110.dp)
                    .padding(end = 12.dp),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = pelicula.title ?: "Sin título",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📅 Estreno: ${pelicula.releaseDate ?: "No disponible"}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "⭐ Rating: ${pelicula.voteAverage ?: "N/A"}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun PeliculaTrendingItem(
    pelicula: Peliculas,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .size(width = 180.dp, height = 240.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            val imageUrl = pelicula.posterPath
                ?.let { "https://image.tmdb.org/t/p/w500$it" }
                ?: "https://via.placeholder.com/180x200"
            AsyncImage(
                model = imageUrl,
                contentDescription = pelicula.title ?: "Sin nombre",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(8.dp))
            Column(Modifier.padding(horizontal = 8.dp)) {
                Text(
                    text = pelicula.title ?: "Desconocida",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Estreno: ${pelicula.releaseDate ?: "No disponible"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Rating: ${pelicula.voteAverage ?: "N/A"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun PeliculaMiListaItem(
    pelicula: Peliculas,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .padding(6.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${pelicula.posterPath}",
                contentDescription = pelicula.title ?: "Película sin título",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = pelicula.title ?: "Sin título",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar",
                    tint = Color.Red
                )
            }
        }
    }
}


