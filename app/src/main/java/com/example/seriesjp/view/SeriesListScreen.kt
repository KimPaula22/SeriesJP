package com.example.seriesjp.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seriesjp.R
import com.example.seriesjp.model.Series
import com.example.seriesjp.viewmodel.SeriesViewModel

@Composable
fun SeriesListScreen(viewModel: SeriesViewModel) {
    // Usamos el estado del ViewModel para obtener la lista de series
    val seriesList = viewModel.seriesList

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Título de la pantalla
        Text(
            text = "Series",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )

        // Si no hay datos, mostramos un indicador de carga
        if (seriesList.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.fillMaxSize())
        } else {
            // Mostrar la lista de series en una LazyColumn
            LazyColumn {
                // Iteramos sobre la lista de series
                items(seriesList) { series ->
                    // Llamamos al composable SeriesItem para cada serie
                    SeriesItem(series = series)
                }
            }
        }
    }
}

@Composable
fun SeriesItem(series: Series) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .clickable {
                // Acción de clic si es necesario
            }
    ) {
        // Asignar el drawable dependiendo de la serie
        val imageResId = when (series.name) {
            "Breaking Bad" -> R.drawable.breakingbad
            "Stranger Things" -> R.drawable.strangerthings
            "Game of Thrones" -> R.drawable.gameofthrones
            else -> R.drawable.default_series // Imagen predeterminada
        }

        // Cargar el drawable desde los recursos locales
        val painter = painterResource(id = imageResId)

        // Mostrar la imagen (drawable)
        Image(
            painter = painter,
            contentDescription = series.name,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))

        // Información de la serie
        Column {
            Text(text = series.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "Rating: ${series.voteAverage}")
            Text(text = "Release Date: ${series.firstAirDate}")
        }
    }
}