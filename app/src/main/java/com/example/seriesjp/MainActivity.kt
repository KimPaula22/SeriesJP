package com.example.seriesjp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.seriesjp.ui.theme.SeriesJPTheme
import com.example.seriesjp.view.PeliculaDetailsScreen
import com.example.seriesjp.view.PeliculasListScreen
import com.example.seriesjp.view.PeliculaTrendingItem
import com.example.seriesjp.view.PeliculaItem
import com.example.seriesjp.view.PeliculaMiListaItem
import com.example.seriesjp.view.SeriesDetailsScreen
import com.example.seriesjp.view.SeriesListScreen
import com.example.seriesjp.view.SeriesItem
import com.example.seriesjp.view.SeriesTrendingItem
import com.example.seriesjp.view.SerieMiListaItem
import com.example.seriesjp.viewmodel.PeliculasViewModel
import com.example.seriesjp.viewmodel.SeriesViewModel
import com.example.seriesjp.R

class MainActivity : ComponentActivity() {

    private val seriesViewModel: SeriesViewModel by viewModels()
    private val peliculasViewModel: PeliculasViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeriesJPTheme {
                MainScreen(
                    navController = rememberNavController(),
                    viewModel = seriesViewModel,
                    peliculasViewModel = peliculasViewModel
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: SeriesViewModel,
    peliculasViewModel: PeliculasViewModel
) {
    // Estado de búsqueda
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
    }

    fun onSearchClick() {
        isSearching = !isSearching
        if (!isSearching) {
            searchQuery = ""
        }
    }

    fun onRefreshClick() {
        viewModel.refreshSeries()
        peliculasViewModel.refreshPeliculas()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            isSearching = isSearching,
            searchQuery = searchQuery,
            onSearchQueryChange = { onSearchQueryChange(it) },
            onSearchClick = { onSearchClick() },
            onSearchSubmit = {
                viewModel.searchSeries(searchQuery)
                peliculasViewModel.searchPeliculas(searchQuery)
            },
            onRefreshClick = { onRefreshClick() }
        )
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
fun TopBar(
    isSearching: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onSearchSubmit: () -> Unit,
    onRefreshClick: () -> Unit
) {
    TopAppBar(
        title = {
            if (isSearching) {
                TextField(
                    value = searchQuery,
                    onValueChange = { onSearchQueryChange(it) },
                    label = { Text("Buscar...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { onSearchSubmit() }
                    )
                )
            } else {
                Text(
                    text = "BEST S&M",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(3f, 3f),
                            blurRadius = 6f
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        actions = {
            if (isSearching) {
                IconButton(onClick = { onSearchClick() }) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar búsqueda", tint = Color.White)
                }
            } else {
                IconButton(onClick = { onSearchClick() }) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.White)
                }
                IconButton(onClick = { onRefreshClick() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF162447) // 🔵 Fondo azul oscuro
        )
    )
}

@Composable
fun BottomNav(navController: NavHostController) {
    BottomAppBar(
        containerColor = Color(0xFF1B1B2F),
        contentColor = Color.White,
        tonalElevation = 8.dp
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            IconButton(onClick = { navController.navigate("home") }) {
                Icon(Icons.Default.Home, contentDescription = "Inicio", tint = Color(0xFFE43F5A))
            }
            IconButton(onClick = { navController.navigate("series") }) {
                Icon(Icons.Default.List, contentDescription = "Series", tint = Color(0xFFF0A500))
            }
            IconButton(onClick = { navController.navigate("peliculas") }) {
                Icon(
                    painter = painterResource(id = R.drawable.movie),
                    contentDescription = "Películas",
                    tint = Color(0xFF17D7A0)
                )
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: SeriesViewModel,
    peliculasViewModel: PeliculasViewModel
) {
    NavHost(navController = navController, startDestination = "home", modifier = modifier) {
        composable("home") {
            HomeScreen(
                navController = navController,
                viewModel = viewModel,
                peliculasViewModel = peliculasViewModel
            )
        }
        composable("series") {
            SeriesListScreen(viewModel = viewModel, navController = navController)
        }
        composable("seriesDetails/{seriesId}") { backStackEntry ->
            val seriesId = backStackEntry.arguments?.getString("seriesId")?.toIntOrNull()
            if (seriesId != null) {
                SeriesDetailsScreen(
                    navController = navController,
                    seriesId = seriesId,
                    viewModel = viewModel
                )
            }
        }
        composable("peliculas") {
            PeliculasListScreen(viewModel = peliculasViewModel, navController = navController)
        }
        composable("peliculaDetails/{peliculaId}") { backStackEntry ->
            val peliculaId = backStackEntry.arguments?.getString("peliculaId")?.toIntOrNull()
            if (peliculaId != null) {
                PeliculaDetailsScreen(
                    navController = navController,
                    peliculaId = peliculaId,
                    peliculasViewModel = peliculasViewModel
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: SeriesViewModel,
    peliculasViewModel: PeliculasViewModel
) {
    val popularSeries = viewModel.seriesList.value
    val popularPeliculas = peliculasViewModel.peliculasList.value
    val miListaSeries = viewModel.miListaSeries
    val miListaPeliculas = peliculasViewModel.miListaPeliculas

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient( //
                    colors = listOf(Color(0xFF1B1B2F), Color(0xFF162447))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ✅ Título con sombra corregido
            Text(
                text = "Bienvenido a la app",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                style = TextStyle( //
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(5f, 5f),
                        blurRadius = 8f
                    )
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Tendencias en Series")
            if (popularSeries.isEmpty()) {
                CircularProgressIndicator()
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(popularSeries) { serie ->
                        SeriesTrendingItem(
                            series = serie,
                            onClick = { navController.navigate("seriesDetails/${serie.id}") },
                            viewModel = viewModel
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Tendencias en Películas")
            if (popularPeliculas.isEmpty()) {
                CircularProgressIndicator()
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(popularPeliculas) { pelicula ->
                        PeliculaTrendingItem(
                            pelicula = pelicula,
                            onClick = { navController.navigate("peliculaDetails/${pelicula.id}") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Mi Lista")
            if (miListaSeries.isEmpty() && miListaPeliculas.isEmpty()) {
                Text("No has añadido nada a tu lista.", color = Color.Gray)
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(miListaSeries) { serie ->
                        SerieMiListaItem(
                            serie = serie,
                            onRemove = { viewModel.quitarSerieDeMiLista(serie) }
                        )
                    }
                    items(miListaPeliculas) { pelicula ->
                        PeliculaMiListaItem(
                            pelicula = pelicula,
                            onRemove = { peliculasViewModel.quitarPeliculaDeMiLista(pelicula) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = Color.LightGray
    )
    Spacer(modifier = Modifier.height(8.dp))
}