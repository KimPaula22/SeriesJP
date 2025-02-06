package com.example.seriesjp.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.example.seriesjp.model.Series
import com.example.seriesjp.viewmodel.SeriesViewModel

@Composable
fun SeriesDetailsScreen(
    navController: NavHostController,
    seriesId: Int?,
    viewModel: SeriesViewModel
) {
    val seriesList = viewModel.seriesList.value
    val serie = seriesId?.let { id -> seriesList.find { it.id == id } }
    val enMiLista = serie != null && viewModel.miListaSeries.contains(serie)

    if (serie != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
            }
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${serie.posterPath}",
                contentDescription = serie.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(bottom = 16.dp)
            )
            Text(
                text = serie.name,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(text = "Fecha de estreno: ${serie.firstAirDate}", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (enMiLista) {
                        viewModel.quitarSerieDeMiLista(serie)
                    } else {
                        viewModel.agregarSerieAMiLista(serie)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (enMiLista) "Quitar de Mi Lista" else "Añadir a Mi Lista")
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Serie no encontrada")
        }
    }
}
