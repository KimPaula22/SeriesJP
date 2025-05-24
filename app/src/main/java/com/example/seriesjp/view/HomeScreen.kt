package com.example.seriesjp.view

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.seriesjp.viewmodel.PeliculasViewModel
import com.example.seriesjp.viewmodel.SeriesViewModel
import androidx.compose.material3.Text

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: SeriesViewModel,
    peliculasViewModel: PeliculasViewModel
) {
    // Puedes personalizar esto después con más contenido
    Text(text = "Bienvenido a la pantalla principal")
}