package com.example.seriesjp.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.seriesjp.model.Series
import com.example.seriesjp.viewmodel.SeriesViewModel
import com.example.seriesjp.model.Provider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.text.font.FontWeight

@Composable
fun BackButton(navController: NavHostController) {
    IconButton(onClick = {
        if (!navController.popBackStack()) {
            navController.navigate("home") {
                popUpTo("home") { inclusive = false }
            }
        }
    }) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
    }
}
@Composable
fun RatingBar(
    currentRating: Int,
    onRatingSelected: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..10) {
            IconButton(onClick = { onRatingSelected(i) }) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Puntuación $i",
                    tint = if (i <= currentRating) Color.Yellow else Color.Gray
                )
            }
        }
    }
}

@Composable
fun SeriesDetailsScreen(
    navController: NavHostController,
    seriesId: Int?,
    viewModel: SeriesViewModel
) {
    val populares by viewModel.seriesList
    val recomendaciones by viewModel.recommendedSeries
    val providers by viewModel.watchProviders

    val serie: Series? = seriesId?.let { id ->
        populares.find { it.id == id } ?: recomendaciones.find { it.id == id }
    }

    val enMiLista = serie != null && viewModel.miListaSeries.contains(serie)

    serie?.let { s ->
        // Cargar puntuación solo una vez al entrar
        LaunchedEffect(s.id) {
            viewModel.loadWatchProviders(s.id)
            viewModel.cargarRecomendaciones(s.id)
            viewModel.cargarPuntuacion(s.id)
        }
    }

    val userRating = viewModel.ratings[serie?.id] ?: 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF4C6D7), Color(0xFF121212))
                )
            )
            .padding(16.dp)
    ) {
        serie?.let {
            Column(modifier = Modifier.fillMaxSize()) {
                BackButton(navController = navController)

                AsyncImage(
                    model = "https://image.tmdb.org/t/p/w500${it.posterPath}",
                    contentDescription = it.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(bottom = 16.dp),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = it.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "Fecha de estreno: ${it.firstAirDate}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Media de puntuación
                Text(
                    text = "Puntuación media: ${String.format("%.1f", it.voteAverage)} / 10",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Barra de puntuación del usuario
                Text(
                    text = "Tu puntuación:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                RatingBar(
                    currentRating = userRating,
                    onRatingSelected = { rating ->
                        serie?.let { serie ->
                            viewModel.guardarPuntuacion(serie.id, rating)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (providers.isNotEmpty()) {
                    Text(
                        text = "Dónde verla en España:",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(providers) { prov ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(64.dp)
                            ) {
                                AsyncImage(
                                    model = "https://image.tmdb.org/t/p/w92${prov.logoPath}",
                                    contentDescription = prov.providerName,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = prov.providerName,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(top = 4.dp),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Button(
                    onClick = {
                        if (enMiLista) viewModel.quitarSerieDeMiLista(it)
                        else viewModel.agregarSerieAMiLista(it)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (enMiLista) "Quitar de Mi Lista" else "Añadir a Mi Lista")
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Serie no encontrada",
                    color = Color.White
                )
            }
        }
    }
}
