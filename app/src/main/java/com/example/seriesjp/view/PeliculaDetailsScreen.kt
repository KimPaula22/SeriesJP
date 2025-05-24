// PeliculaDetailsScreen.kt
package com.example.seriesjp.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.seriesjp.model.Peliculas
import com.example.seriesjp.viewmodel.PeliculasViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.font.FontWeight

@Composable
fun PeliculaDetailsScreen(
    navController: NavController,
    peliculaId: Int?,
    viewModel: PeliculasViewModel
) {
    // Obtener listas
    val populares by viewModel.peliculasList
    val recomendadas by viewModel.recommendedPeliculas

    // Buscar la película primero en populares, luego en recomendadas
    val pelicula: Peliculas? = peliculaId?.let { id ->
        populares.find { it.id == id }
            ?: recomendadas.find { it.id == id }
    }

    // Cargar proveedores al cambiar el ID
    LaunchedEffect(peliculaId) {
        peliculaId?.let { viewModel.loadWatchProviders(it) }
    }
    val providers by viewModel.watchProviders

    pelicula?.let { p ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Botón volver
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
            }

            // Imagen
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${p.posterPath}",
                contentDescription = p.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Crop
            )

            // Info básica
            Text(p.title, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Spacer(Modifier.height(8.dp))
            Text("Descripción:", fontWeight = FontWeight.Bold)
            Text(p.overview)
            Spacer(Modifier.height(8.dp))
            Text("Fecha de estreno: ${p.releaseDate}")
            Spacer(Modifier.height(8.dp))
            Text("Puntuación: ${p.voteAverage}")

            // Proveedores
            if (providers.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("Dónde verla en España:", style = MaterialTheme.typography.titleMedium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    items(providers) { prov ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(80.dp)
                        ) {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w92${prov.logoPath}",
                                contentDescription = prov.providerName,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(prov.providerName, fontSize = 12.sp, maxLines = 2)
                        }
                    }
                }
            }

            // Mi Lista
            Spacer(Modifier.height(16.dp))
            val enMiLista = viewModel.miListaPeliculas.contains(p)
            Button(
                onClick = {
                    if (enMiLista) viewModel.quitarPeliculaDeMiLista(p)
                    else viewModel.agregarPeliculaAMiLista(p)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (enMiLista) "Quitar de Mi Lista" else "Añadir a Mi Lista")
            }
        }
    } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Película no encontrada")
    }
}
