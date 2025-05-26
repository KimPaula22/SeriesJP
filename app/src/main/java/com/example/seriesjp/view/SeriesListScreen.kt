package com.example.seriesjp.view

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import com.example.seriesjp.model.Series
import com.example.seriesjp.viewmodel.SeriesViewModel

@Composable
fun SeriesListScreen(
    viewModel: SeriesViewModel,
    navController: NavHostController
) {
    val seriesList = viewModel.seriesList.value
    val recommendedSeries = viewModel.recommendedSeries.value
    val isLoading = seriesList.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Series Populares",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Lista principal de series
                items(seriesList) { serie ->
                    SeriesItem(
                        series = serie,
                        onClick = {
                            // Primero cargamos recomendaciones de esta serie
                            viewModel.cargarRecomendaciones(serie.id)
                            // Luego navegamos al detalle
                            navController.navigate("seriesDetails/${serie.id}")
                        }
                    )
                }

                // Sección de recomendaciones
                item {
                    if (recommendedSeries.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Recomendaciones",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(recommendedSeries) { recSerie ->
                                SeriesTrendingItem(
                                    series = recSerie,
                                    onClick = {
                                        // Igual aquí: cargar y navegar
                                        viewModel.cargarRecomendaciones(recSerie.id)
                                        navController.navigate("seriesDetails/${recSerie.id}")
                                    },
                                    viewModel = viewModel
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
fun SeriesItem(
    series: Series,
    onClick: () -> Unit
) {
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
                model = "https://image.tmdb.org/t/p/w500${series.posterPath}",
                contentDescription = series.name,
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 8.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = series.name ?: "Sin título",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Text(
                    text = "📅 Estreno: ${series.firstAirDate ?: "No disponible"}",
                    fontSize = 12.sp
                )
                Text(
                    text = "⭐ Rating: ${series.voteAverage ?: "N/A"}",
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun SeriesTrendingItem(
    series: Series,
    onClick: () -> Unit = {},
    viewModel: SeriesViewModel
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            val imageUrl = series.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
                ?: "https://via.placeholder.com/180x240"
            AsyncImage(
                model = imageUrl,
                contentDescription = series.name ?: "Sin nombre",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(
                    text = series.name ?: "Desconocida",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2
                )
                Text(
                    text = "Estreno: ${series.firstAirDate ?: "No disponible"}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Rating: ${series.voteAverage ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun SerieMiListaItem(
    serie: Series,
    onRemove: () -> Unit
) {
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
                model = "https://image.tmdb.org/t/p/w500${serie.posterPath}",
                contentDescription = serie.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = serie.name,
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
