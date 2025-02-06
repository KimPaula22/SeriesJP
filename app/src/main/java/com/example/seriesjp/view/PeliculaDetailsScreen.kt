package com.example.seriesjp.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.font.FontWeight
import com.example.seriesjp.model.Peliculas
import com.example.seriesjp.viewmodel.PeliculasViewModel

@Composable
fun PeliculaDetailsScreen(
    navController: NavController,
    peliculaId: Int?,
    peliculasViewModel: PeliculasViewModel
) {
    // Se obtiene la lista actual de películas y se busca la película por id.
    val peliculas = peliculasViewModel.peliculasList.value
    val pelicula = peliculas.find { it.id == peliculaId }

    if (pelicula != null) {
        // Verifica si la película ya está en "Mi Lista"
        val enMiLista = pelicula in peliculasViewModel.miListaPeliculas

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Botón para volver atrás
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
            }

            // Imagen de la película
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${pelicula.posterPath}",
                contentDescription = pelicula.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(bottom = 16.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
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
            Text(
                text = pelicula.overview,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Fecha de estreno
            Text(
                text = "Fecha de estreno: ${pelicula.releaseDate}",
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Puntuación
            Text(
                text = "Puntuación: ${pelicula.voteAverage}",
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Botón para agregar o quitar de "Mi Lista"
            Button(
                onClick = {
                    if (enMiLista) {
                        peliculasViewModel.quitarPeliculaDeMiLista(pelicula)
                    } else {
                        peliculasViewModel.agregarPeliculaAMiLista(pelicula)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (enMiLista) "Quitar de Mi Lista" else "Añadir a Mi Lista",
                    fontSize = 16.sp
                )
            }
        }
    } else {
        // Mensaje de error si no se encuentra la película
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Película no encontrada")
        }
    }
}
