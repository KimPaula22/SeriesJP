package com.example.seriesjp.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.seriesjp.model.Pelicula
import com.example.seriesjp.viewmodel.PeliculasViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage

@Composable
fun PeliculaDetailsScreen(
    navController: NavController,
    peliculaId: Int?,
    peliculasViewModel: PeliculasViewModel
) {
    val peliculas = peliculasViewModel.peliculasList.value
    val pelicula = peliculas.find { it.id == peliculaId } // `id` viene de la API

    if (pelicula != null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            // Botón de volver atrás
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
            }

            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${pelicula.posterPath}",
                contentDescription = pelicula.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(bottom = 16.dp)
            )

            // Título de la película
            Text(
                text = pelicula.title,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Descripción de la película
            Text(text = "Descripción:", fontWeight = FontWeight.Bold)
            Text(text = pelicula.overview, modifier = Modifier.padding(bottom = 8.dp))

            // Fecha de lanzamiento
            Text(text = "Fecha de estreno: ${pelicula.releaseDate}", modifier = Modifier.padding(bottom = 8.dp))

            // Puntuación
            Text(text = "Puntuación: ${pelicula.voteAverage}", modifier = Modifier.padding(bottom = 8.dp))
        }
    } else {
        // Mostrar un mensaje si no se encuentra la película
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Película no encontrada")
        }
    }
}
