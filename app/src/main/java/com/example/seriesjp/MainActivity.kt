package com.example.seriesjp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.seriesjp.ui.theme.SeriesJPTheme
import com.example.seriesjp.view.SeriesListScreen
import com.example.seriesjp.viewmodel.SeriesViewModel
import com.example.seriesjp.R
import com.example.seriesjp.view.PeliculaDetailsScreen
import com.example.seriesjp.viewmodel.PeliculasViewModel

class MainActivity : ComponentActivity() {

    // Usar el ViewModel
    private val seriesViewModel: SeriesViewModel by viewModels()
    private val peliculasViewModel: PeliculasViewModel by viewModels() // Agregar el ViewModel de Peliculas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeriesJPTheme {
                // Pantalla principal con barra de navegación y contenido
                MainScreen(
                    viewModel = seriesViewModel,
                    peliculasViewModel= peliculasViewModel // Pasamos ambos ViewModels
                )
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: SeriesViewModel, peliculasViewModel: PeliculasViewModel) {
    val navController = rememberNavController()

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar()
        AppNavHost(
            navController = navController,
            modifier = Modifier.weight(1f),
            viewModel = viewModel,
            peliculasViewModel = peliculasViewModel
        )
        BottomNav(navController = navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    TopAppBar(
        title = { Text("Series y Películas") },
        actions = {
            IconButton(onClick = { /* Acción de búsqueda */ }) {
                Icon(Icons.Default.Search, contentDescription = "Buscar")
            }
            IconButton(onClick = { /* Acción de ajustes */ }) {
                Icon(Icons.Default.Settings, contentDescription = "Ajustes")
            }
        }
    )
}

@Composable
fun BottomNav(navController: NavHostController) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        // Botón de inicio
        IconButton(onClick = { navController.navigate("home") }) {
            Icon(Icons.Default.Home, contentDescription = "Inicio")
        }

        // Botón de series
        IconButton(onClick = { navController.navigate("series") }) {
            Icon(Icons.Default.List, contentDescription = "Series")
        }

        // Botón de películas con ícono personalizado
        IconButton(onClick = { navController.navigate("peliculas") }) {
            // Cargar el ícono desde los recursos
            val peliculasIcon = painterResource(id = R.drawable.movie)
            Icon(painter = peliculasIcon, contentDescription = "Películas")
        }
    }
}
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier,
    viewModel: SeriesViewModel,
    peliculasViewModel: PeliculasViewModel
) {
    NavHost(navController = navController, startDestination = "home", modifier = modifier) {
        composable("home") { HomeScreen() }
        composable("series") {
            SeriesListScreen(viewModel = viewModel) // Pasa SeriesViewModel
        }
        composable("peliculas") {
            PeliculasListScreen(peliculasViewModel = peliculasViewModel, navController = navController) // Pasa PeliculasViewModel y navController
        }
        composable("peliculaDetails/{peliculaId}") { backStackEntry ->
            val peliculaId = backStackEntry.arguments?.getString("peliculaId")?.toIntOrNull()
            if (peliculaId != null) {
                PeliculaDetailsScreen(
                    navController = navController, // Pasa navController
                    peliculaId = peliculaId, // Pasa peliculaId
                    peliculasViewModel = peliculasViewModel // Pasa peliculasViewModel
                )
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Bienvenido a la app")
        Spacer(modifier = Modifier.height(16.dp))

        // Aquí puedes agregar una lista de series/películas recomendadas
        Text("Recomendaciones")
    }
}

@Composable
fun PeliculasListScreen(peliculasViewModel: PeliculasViewModel, navController: NavHostController) {
    // Aquí va la UI para mostrar las películas
    Text("Pantalla de Películas")
    // Puedes usar peliculasViewModel para obtener datos o interactuar con la lógica
}
