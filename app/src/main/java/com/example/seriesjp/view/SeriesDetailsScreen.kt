// SeriesDetailsScreen.kt
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
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.seriesjp.model.Provider
import com.example.seriesjp.model.Series
import com.example.seriesjp.viewmodel.SeriesViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.font.FontWeight

@Composable
fun SeriesDetailsScreen(
    navController: NavHostController,
    seriesId: Int?,
    viewModel: SeriesViewModel
) {
    val seriesList by viewModel.seriesList
    val serie: Series? = seriesId?.let { id -> seriesList.find { it.id == id } }
    val enMiLista = serie != null && viewModel.miListaSeries.contains(serie)
    val providers by viewModel.watchProviders

    serie?.let {
        // Carga proveedores y recomendaciones al entrar
        LaunchedEffect(it.id) {
            viewModel.loadWatchProviders(it.id)
            viewModel.cargarRecomendaciones(it.id)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Botón de vuelta
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
            }

            // Póster
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${it.posterPath}",
                contentDescription = it.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Crop
            )

            // Título y fecha
            Text(
                text = it.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Fecha de estreno: ${it.firstAirDate}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Proveedores de streaming
            if (providers.isNotEmpty()) {
                Text(
                    text = "Dónde verla en España:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(providers) { prov: Provider ->
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
                                modifier = Modifier.padding(top = 4.dp),
                                maxLines = 2
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Botón Mi Lista
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
            Text("Serie no encontrada")
        }
    }
}
