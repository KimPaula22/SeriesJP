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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.seriesjp.datastore.SessionPreferences
import com.example.seriesjp.ui.theme.SeriesJPTheme
import com.example.seriesjp.view.*
import com.example.seriesjp.viewmodel.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreSettings

class MainActivity : ComponentActivity() {
    private val seriesViewModel: SeriesViewModel by viewModels()
    private val peliculasViewModel: PeliculasViewModel by viewModels()
    private val favoritesViewModel: FavoritesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        setContent {
            SeriesJPTheme {
                val context = this
                val sessionPreferences = SessionPreferences(context)
                val authViewModel: AuthViewModel =
                    viewModel(factory = AuthViewModelFactory(sessionPreferences))
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {
                    // 1) Splash: pantalla inicial que decide dónde ir
                    composable("splash") {
                        SplashScreen(authViewModel = authViewModel, navController = navController)
                    }
                    // 2) Login
                    composable("login") {
                        LoginScreen(navController = navController) { uid ->
                            Log.d("MainActivity", "Login manual exitoso, uid=$uid")
                            // Cargar lista desde Firestore si hace falta
                            peliculasViewModel.cargarMiListaDesdeFirestore(uid)
                            // Navegar a home
                            navController.navigate("home/$uid") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                    // 3) Register
                    composable("register") {
                        RegisterScreen(navController = navController) { uid ->
                            Log.d("MainActivity", "Registro exitoso, uid=$uid")
                            peliculasViewModel.cargarMiListaDesdeFirestore(uid)
                            navController.navigate("home/$uid") {
                                popUpTo("register") { inclusive = true }
                            }
                        }
                    }
                    // 4) Home
                    composable(
                        "home/{userId}",
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId")
                        if (userId != null) {
                            // Carga inicial de datos justo al entrar
                            LaunchedEffect(userId) {
                                Log.d("HomeScreen", "Entrando a HomeScreen con userId=$userId")
                                seriesViewModel.refreshSeries()
                                peliculasViewModel.refreshPeliculas()
                                favoritesViewModel.loadFavorites(userId)
                            }
                            HomeScreen(
                                navController = navController,
                                seriesViewModel = seriesViewModel,
                                peliculasViewModel = peliculasViewModel,
                                favoritesViewModel = favoritesViewModel,
                                userId = userId
                            )
                        }
                    }
                    // 5) Rutas secundarias: series, detalles, etc.
                    composable("series") {
                        SeriesListScreen(viewModel = seriesViewModel, navController = navController)
                    }
                    composable(
                        "seriesDetails/{seriesId}",
                        arguments = listOf(navArgument("seriesId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val seriesId = backStackEntry.arguments?.getInt("seriesId")
                        // Mejor pasar userId también, en vez de FirebaseAuth.currentUser
                        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                        if (seriesId != null && currentUserId != null) {
                            SeriesDetailsScreen(
                                navController = navController,
                                seriesId = seriesId,
                                viewModel = seriesViewModel,
                                favoritesViewModel = favoritesViewModel,
                                userId = currentUserId
                            )
                        }
                    }
                    composable("peliculas") {
                        PeliculasListScreen(viewModel = peliculasViewModel, navController = navController)
                    }
                    composable(
                        "peliculaDetails/{peliculaId}/{userId}",
                        arguments = listOf(
                            navArgument("peliculaId") { type = NavType.IntType },
                            navArgument("userId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val peliculaId = backStackEntry.arguments?.getInt("peliculaId")
                        val userId = backStackEntry.arguments?.getString("userId")
                        if (peliculaId != null && userId != null) {
                            PeliculaDetailsScreen(
                                navController = navController,
                                peliculaId = peliculaId,
                                viewModel = peliculasViewModel,
                                favoritesViewModel = favoritesViewModel,
                                userId = userId
                            )
                        }
                    }
                    composable(
                        "favorites/{userId}",
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId")
                        if (userId != null) {
                            FavoritesScreen(
                                userId = userId,
                                favoritesViewModel = favoritesViewModel,
                                navController = navController
                            )
                        }
                    }
                    composable(
                        "profile/{userId}",
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId")
                        if (userId != null) {
                            ProfileScreen(navController = navController, userId = userId) {
                                Log.d("MainActivity", "Cerrar sesión solicitado")
                                authViewModel.signOut()
                                navController.navigate("login") {
                                    popUpTo("home/$userId") { inclusive = true }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Estado de splash
    sealed class AutoLoginState {
        object Checking : AutoLoginState()
        object Success : AutoLoginState()
        object Failed : AutoLoginState()
    }

    // SplashScreen Composable:
    @Composable
    fun SplashScreen(
        authViewModel: AuthViewModel,
        navController: NavHostController
    ) {
        // Estado interno para mostrar loader
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
        // Al lanzarse el Composable, comprobar auto-login:
        LaunchedEffect(Unit) {
            Log.d("SplashScreen", "Iniciando checkAutoLogin desde SplashScreen")
            authViewModel.checkAutoLogin(
                onAutoLoginSuccess = { uid ->
                    Log.d("SplashScreen", "Auto login success: uid=$uid")
                    // Cargar lista si hace falta
                    peliculasViewModel.cargarMiListaDesdeFirestore(uid)
                    navController.navigate("home/$uid") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onAutoLoginFail = {
                    Log.d("SplashScreen", "Auto login fail -> navegar a login")
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
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
                            shadow = Shadow(Color.Black, Offset(3f, 3f), 6f)
                        )
                    )
                }
            },
            actions = {
                if (isSearching) {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                } else {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = onRefreshClick) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF162447))
        )
    }

    @Composable
    fun BottomNav(navController: NavHostController, userId: String) {
        BottomAppBar(containerColor = Color(0xFF1B1B2F)) {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                IconButton(onClick = { navController.navigate("home/$userId") }) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Inicio",
                        tint = Color(0xFFE43F5A)
                    )
                }
                IconButton(onClick = { navController.navigate("series") }) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = "Series",
                        tint = Color(0xFFF0A500)
                    )
                }
                IconButton(onClick = { navController.navigate("peliculas") }) {
                    Icon(
                        painter = painterResource(R.drawable.movie),
                        contentDescription = "Películas",
                        tint = Color(0xFF17D7A0)
                    )
                }
                IconButton(onClick = { navController.navigate("favorites/$userId") }) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Favoritos",
                        tint = Color(0xFFFF4081)
                    )
                }
            }
        }
    }

    @Composable
    fun HomeScreen(
        navController: NavHostController,
        seriesViewModel: SeriesViewModel,
        peliculasViewModel: PeliculasViewModel,
        favoritesViewModel: FavoritesViewModel,
        userId: String
    ) {
        var searchQuery by remember { mutableStateOf("") }
        var isSearching by remember { mutableStateOf(false) }

        // Observa los favoritos desde el ViewModel
        val favoritesList by favoritesViewModel.favorites.collectAsState()

        // Carga los favoritos al iniciar o cuando cambia userId
        LaunchedEffect(userId) {
            favoritesViewModel.loadFavorites(userId)
        }

        Scaffold(
            topBar = {
                TopBar(
                    isSearching = isSearching,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onSearchClick = {
                        isSearching = !isSearching
                        if (!isSearching) {
                            searchQuery = ""
                            seriesViewModel.refreshSeries()
                            peliculasViewModel.refreshPeliculas()
                        }
                    },
                    onSearchSubmit = {
                        if (searchQuery.isBlank()) {
                            seriesViewModel.refreshSeries()
                            peliculasViewModel.refreshPeliculas()
                        } else {
                            seriesViewModel.searchSeries(searchQuery)
                            peliculasViewModel.searchPeliculas(searchQuery)
                        }
                    },
                    onRefreshClick = {
                        seriesViewModel.refreshSeries()
                        peliculasViewModel.refreshPeliculas()
                    },
                    onProfileClick = {
                        navController.navigate("profile/$userId")
                    }
                )
            },
            bottomBar = {
                BottomNav(navController = navController, userId = userId)
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1B1B2F), Color(0xFF162447))
                        )
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Tendencias Series
                Text(
                    text = "Tendencias en Series",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(seriesViewModel.seriesList.value) { serie ->
                        SeriesTrendingItem(serie) {
                            navController.navigate("seriesDetails/${serie.id}")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tendencias Películas
                Text(
                    text = "Tendencias en Películas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(peliculasViewModel.peliculasList.value) { pelicula ->
                        PeliculaTrendingItem(pelicula) {
                            navController.navigate("peliculaDetails/${pelicula.id}/$userId")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // MiLista - Favoritos
                Text(
                    text = "Mi Lista",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (favoritesList.isEmpty()) {
                    Text(
                        text = "No tienes favoritos guardados.",
                        color = Color.LightGray,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(favoritesList) { favorito ->
                            Card(
                                modifier = Modifier
                                    .size(width = 160.dp, height = 240.dp)
                                    .padding(8.dp)
                                    .clickable {
                                        if (favorito.tipo == "serie") {
                                            navController.navigate("seriesDetails/${favorito.id}")
                                        } else if (favorito.tipo == "pelicula") {
                                            navController.navigate("peliculaDetails/${favorito.id}/$userId")
                                        }
                                    },
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column {
                                    favorito.posterUrl?.let { imageUrl ->
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = favorito.titulo,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp),
                                        maxLines = 2
                                    )
                                }
                            }
                        }

                    }
                }
            }
        }
    }
