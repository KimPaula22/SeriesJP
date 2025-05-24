package com.example.seriesjp.view

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.seriesjp.model.Peliculas
import com.example.seriesjp.viewmodel.PeliculasViewModel

@Composable
fun PeliculasListScreen(viewModel: PeliculasViewModel, navController: NavHostController) {
    val peliculasList = viewModel.peliculasList.value
    val recommendedPeliculas = viewModel.recommendedPeliculas.value
    val isLoading = peliculasList.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Películas Populares",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // RECOMENDACIONES EN HORIZONTAL
        if (recommendedPeliculas.isNotEmpty()) {
            Text(
                text = "Recomendaciones para ti",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(recommendedPeliculas) { pelicula ->
                    PeliculaTrendingItem(
                        pelicula = pelicula,
                        onClick = {
                            // cargamos sus recomendaciones
                            viewModel.cargarRecomendaciones(pelicula.id)
                            navController.navigate("peliculaDetails/${pelicula.id}")
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(peliculasList) { pelicula ->
                    PeliculaItem(
                        pelicula = pelicula,
                        onClick = {
                            // al pulsar una película del listado, también cargamos recomendaciones
                            viewModel.cargarRecomendaciones(pelicula.id)
                            navController.navigate("peliculaDetails/${pelicula.id}")
                        },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun PeliculaItem(
    pelicula: Peliculas,
    onClick: () -> Unit,
    viewModel: PeliculasViewModel
) {
    val yaEnLista = viewModel.miListaPeliculas.contains(pelicula)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${pelicula.posterPath}",
                contentDescription = pelicula.title,
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 8.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pelicula.title ?: "Sin título",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Text(text = "📅 Estreno: ${pelicula.releaseDate ?: "No disponible"}", fontSize = 12.sp)
                Text(text = "⭐ Rating: ${pelicula.voteAverage ?: "N/A"}", fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (yaEnLista) viewModel.quitarPeliculaDeMiLista(pelicula)
                        else viewModel.agregarPeliculaAMiLista(pelicula)
                    }
                ) {
                    Text(text = if (yaEnLista) "Quitar de Mi Lista" else "Añadir a Mi Lista")
                }
            }
        }
    }
}

@Composable
fun PeliculaTrendingItem(
    pelicula: Peliculas,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            val imageUrl = pelicula.posterPath?.let {
                "https://image.tmdb.org/t/p/w500$it"
            } ?: "https://via.placeholder.com/180x240"

            AsyncImage(
                model = imageUrl,
                contentDescription = pelicula.title ?: "Sin título",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(
                    text = pelicula.title ?: "Desconocida",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2
                )
                Text(
                    text = "Estreno: ${pelicula.releaseDate ?: "No disponible"}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Rating: ${pelicula.voteAverage ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun PeliculaMiListaItem(pelicula: Peliculas, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .padding(4.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${pelicula.posterPath}",
                contentDescription = pelicula.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
            Text(
                text = pelicula.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color.Red)
            }
        }
    }
}
