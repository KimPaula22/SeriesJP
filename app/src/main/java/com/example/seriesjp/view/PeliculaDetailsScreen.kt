package com.example.seriesjp.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.Dp
import com.example.seriesjp.model.Peliculas
import com.example.seriesjp.model.Provider
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

    // Carga de proveedores de streaming
    LaunchedEffect(peliculaId) {
        peliculaId?.let { peliculasViewModel.loadWatchProviders(it) }
    }
    val providers by peliculasViewModel.watchProviders

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
                contentScale = ContentScale.Crop
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

            // Sección de proveedores de streaming
            if (providers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Dónde verla en España:",
                    style = MaterialTheme.typography.titleMedium
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    items(providers) { prov: Provider ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(80.dp)
                        ) {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w92${prov.logoPath}",
                                contentDescription = prov.providerName,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = prov.providerName,
                                fontSize = 12.sp,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            // Botón para agregar o quitar de "Mi Lista"
            Spacer(modifier = Modifier.height(16.dp))
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
