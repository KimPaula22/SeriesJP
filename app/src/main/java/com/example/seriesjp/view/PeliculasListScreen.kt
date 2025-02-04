package com.example.seriesjp.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.seriesjp.R
import com.example.seriesjp.model.Pelicula
import com.example.seriesjp.viewmodel.PeliculasViewModel

@Composable
fun PeliculasListScreen(viewModel: PeliculasViewModel, navController: NavHostController) {
    // Acceder correctamente al valor del State
    val peliculasList = viewModel.peliculasList.value
    val isLoading = peliculasList.isEmpty()  // Para manejar el estado de carga

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Películas",
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
                modifier = Modifier.fillMaxSize()
            ) {
                items(peliculasList) { pelicula ->
                    PeliculaItem(pelicula = pelicula) {
                        // Navegación cuando se hace clic en una película
                        navController.navigate("peliculaDetails/${pelicula.id}")
                    }
                }
            }
        }
    }
}

@Composable
fun PeliculaItem(pelicula: Pelicula, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() } // Ejecuta la función cuando se hace clic
    ) {
        // Usar Coil para cargar imágenes desde URL
        val imagePainter = rememberImagePainter(
            data = pelicula.posterPath,  // Suponiendo que tienes una URL de la imagen en 'posterPath'
            builder = {
                crossfade(true)  // Agregar transición de imagen
            }
        )

        Image(
            painter = imagePainter,
            contentDescription = pelicula.title,
            modifier = Modifier
                .size(100.dp)
                .padding(end = 16.dp)
        )

        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = pelicula.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "Rating: ${pelicula.voteAverage}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Fecha de estreno: ${pelicula.releaseDate}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
