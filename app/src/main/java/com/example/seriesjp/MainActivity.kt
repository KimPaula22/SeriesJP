package com.example.seriesjp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.seriesjp.R
import com.example.seriesjp.ui.theme.SeriesJPTheme
import com.example.seriesjp.view.*
import com.example.seriesjp.viewmodel.FavoritesViewModel
import com.example.seriesjp.viewmodel.PeliculasViewModel
import com.example.seriesjp.viewmodel.SeriesViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class MainActivity : ComponentActivity() {
    private val seriesViewModel: SeriesViewModel by viewModels()
    private val peliculasViewModel: PeliculasViewModel by viewModels()
    private val favoritesViewModel: FavoritesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Firestore offline
        FirebaseFirestore.getInstance().apply {
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        }
        Log.d("Firestore", "Offline enabled")

        setContent {
            SeriesJPTheme {
                val navController = rememberNavController()
                AppNavHost(
                    navController = navController,
                    seriesViewModel = seriesViewModel,
                    peliculasViewModel = peliculasViewModel,
                    favoritesViewModel = favoritesViewModel
                )
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    seriesViewModel: SeriesViewModel,
    peliculasViewModel: PeliculasViewModel,
    favoritesViewModel: FavoritesViewModel,
    startDestination: String = "login"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable(
            "home/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            MainScreen(
                navController = navController,
                seriesViewModel = seriesViewModel,
                peliculasViewModel = peliculasViewModel,
                favoritesViewModel = favoritesViewModel,
                userId = backStackEntry.arguments!!.getString("userId")!!
            )
        }
        composable("series") {
            SeriesListScreen(viewModel = seriesViewModel, navController = navController)
        }
        composable(
            "seriesDetails/{seriesId}",
            arguments = listOf(navArgument("seriesId") { type = NavType.IntType })
        ) { backStackEntry ->
            SeriesDetailsScreen(
                navController = navController,
                seriesId = backStackEntry.arguments!!.getInt("seriesId"),
                viewModel = seriesViewModel
            )
        }
        composable("peliculas") {
            PeliculasListScreen(viewModel = peliculasViewModel, navController = navController)
        }
        composable(
            "peliculaDetails/{peliculaId}",
            arguments = listOf(navArgument("peliculaId") { type = NavType.IntType })
        ) { backStackEntry ->
            PeliculaDetailsScreen(
                navController = navController,
                peliculaId = backStackEntry.arguments!!.getInt("peliculaId"),
                peliculasViewModel = peliculasViewModel
            )
        }
        composable(
            "favorites/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            FavoritesScreen(
                userId = backStackEntry.arguments!!.getString("userId")!!,
                favoritesViewModel = favoritesViewModel
            )
        }
        composable(
            "profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            ProfileScreen(
                navController = navController,
                userId = backStackEntry.arguments!!.getString("userId")!!
            ) {
                navController.navigate("login") {
                    popUpTo("home/${backStackEntry.arguments!!.getString("userId")} ") { inclusive = true }
                }
            }
        }
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
    onRefreshClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    TopAppBar(
        title = {
            if (isSearching) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("Buscar...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() })
                )
            } else {
                Text(
                    text = "BEST S&M",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(3f, 3f),
                            blurRadius = 6f
                        )
                    )
                )
            }
        },
        actions = {
            if (isSearching) {
                IconButton(onClick = onSearchClick) { Icon(Icons.Default.Close, contentDescription = "Cerrar") }
            } else {
                IconButton(onClick = onSearchClick)  { Icon(Icons.Default.Search,  contentDescription = "Buscar") }
                IconButton(onClick = onRefreshClick) { Icon(Icons.Default.Refresh, contentDescription = "Refrescar") }
                IconButton(onClick = onProfileClick) { Icon(Icons.Default.Person,  contentDescription = "Perfil") }
            }
        },
        colors = topAppBarColors(containerColor = Color(0xFF162447))
    )
}

@Composable
fun BottomNav(navController: NavHostController, userId: String) {
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
            IconButton(onClick = { navController.navigate("home/$userId") }) { Icon(Icons.Default.Home, contentDescription = "Inicio", tint = Color(0xFFE43F5A)) }
            IconButton(onClick = { navController.navigate("series")     }) { Icon(Icons.Default.List, contentDescription = "Series", tint = Color(0xFFF0A500)) }
            IconButton(onClick = { navController.navigate("peliculas") }) { Icon(painterResource(R.drawable.movie), contentDescription = "Películas", tint = Color(0xFF17D7A0)) }
        }
    }
}

@Composable
fun MainScreen(
    navController: NavHostController,
    seriesViewModel: SeriesViewModel,
    peliculasViewModel: PeliculasViewModel,
    favoritesViewModel: FavoritesViewModel,
    userId: String
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBar(
                isSearching = isSearching,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchClick = { isSearching = !isSearching; if (!isSearching) searchQuery = "" },
                onSearchSubmit = { seriesViewModel.searchSeries(searchQuery); peliculasViewModel.searchPeliculas(searchQuery) },
                onRefreshClick = { seriesViewModel.refreshSeries(); peliculasViewModel.refreshPeliculas() },
                onProfileClick = { navController.navigate("profile/$userId") }
            )
        },
        bottomBar = { BottomNav(navController, userId) }
    ) { inner ->
        Box(Modifier.padding(inner).fillMaxSize()) {
            HomeScreen(navController, seriesViewModel, peliculasViewModel)
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

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1B1B2F), Color(0xFF162447))))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Tendencias en Series", fontWeight = FontWeight.Bold, fontSize=22.sp, color=Color.LightGray)
        Spacer(Modifier.height(8.dp))
        if (popularSeries.isEmpty()) CircularProgressIndicator() else LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            items(popularSeries) { serie -> SeriesTrendingItem(serie, { navController.navigate("seriesDetails/${serie.id}") }, viewModel) }
        }

        Spacer(Modifier.height(16.dp))
        Text("Tendencias en Películas", fontWeight=FontWeight.Bold, fontSize=22.sp, color=Color.LightGray)
        Spacer(Modifier.height(8.dp))
        if (popularPeliculas.isEmpty()) CircularProgressIndicator() else LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            items(popularPeliculas) { pel -> PeliculaTrendingItem(pel, { navController.navigate("peliculaDetails/${pel.id}") }) }
        }

        Spacer(Modifier.height(16.dp))
        Text("Mi Lista", fontWeight=FontWeight.Bold, fontSize=22.sp, color=Color.LightGray)
        Spacer(Modifier.height(8.dp))
        if (miListaSeries.isEmpty() && miListaPeliculas.isEmpty()) Text("No has añadido nada a tu lista.", color=Color.Gray) else LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            items(miListaSeries) { s->SerieMiListaItem(s){ viewModel.quitarSerieDeMiLista(s) }}
            items(miListaPeliculas) { p->PeliculaMiListaItem(p){ peliculasViewModel.quitarPeliculaDeMiLista(p) }}
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(title, fontWeight = FontWeight.Bold, fontSize=22.sp, color=Color.LightGray)
    Spacer(Modifier.height(8.dp))
}
