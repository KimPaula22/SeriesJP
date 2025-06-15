package com.example.seriesjp.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.seriesjp.model.Series
import com.example.seriesjp.viewmodel.SeriesViewModel
import com.example.seriesjp.model.Provider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.text.font.FontWeight
import com.example.seriesjp.model.Comentario
import com.example.seriesjp.model.Favoritos
import com.example.seriesjp.viewmodel.FavoritesViewModel

@Composable
fun BackButton(navController: NavHostController) {
    IconButton(onClick = {
        if (!navController.popBackStack()) {
            navController.navigate("home") {
                popUpTo("home") { inclusive = false }
            }
        }
    }) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
    }
}

@Composable
fun RatingBar(
    currentRating: Int,
    onRatingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxRating: Int = 10,
    iconSize: Int = 20
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxRating) {
            Icon(
                modifier = Modifier
                    .size(iconSize.dp)
                    .padding(2.dp)
                    .clickable { onRatingSelected(i) },
                imageVector = if (i <= currentRating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Estrella $i",
                tint = Color.Yellow
            )
        }
    }
}

@Composable
fun SeriesDetailsScreen(
    navController: NavHostController,
    seriesId: Int?,
    viewModel: SeriesViewModel,
    favoritesViewModel: FavoritesViewModel,
    userId: String
) {
    val populares by viewModel.seriesList
    val recomendaciones by viewModel.recommendedSeries
    val providers by viewModel.watchProviders

    val serie: Series? = seriesId?.let { id ->
        populares.find { it.id == id } ?: recomendaciones.find { it.id == id }
    }

    val enMiLista = serie != null && viewModel.miListaSeries.contains(serie)

    var showComentariosDialog by remember { mutableStateOf(false) }
    var showNuevoComentarioDialog by remember { mutableStateOf(false) }

    serie?.let { s ->
        LaunchedEffect(s.id) {
            viewModel.loadWatchProviders(s.id)
            viewModel.cargarRecomendaciones(s.id)
            viewModel.cargarPuntuacion(s.id)
            viewModel.cargarComentarios(s.id)
        }
    }

    val userRating = viewModel.ratings[serie?.id] ?: 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF4C6D7), Color(0xFF121212))
                )
            )
            .padding(16.dp)
    ) {
        serie?.let {
            Column(modifier = Modifier.fillMaxSize()) {
                BackButton(navController = navController)

                AsyncImage(
                    model = "https://image.tmdb.org/t/p/w500${it.posterPath}",
                    contentDescription = it.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(bottom = 16.dp),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = it.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "Fecha de estreno: ${it.firstAirDate}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Puntuación media: ${String.format("%.1f", it.voteAverage)} / 10",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Scrollable section
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Tu puntuación:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    RatingBar(
                        currentRating = userRating,
                        onRatingSelected = { rating ->
                            it.id.let { id -> viewModel.guardarPuntuacion(id, rating) }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { showComentariosDialog = true }) {
                            Text("Comentarios")
                        }
                        Button(onClick = { showNuevoComentarioDialog = true }) {
                            Text("Nuevo Comentario")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (providers.isNotEmpty()) {
                        Text(
                            text = "Dónde verla en España:",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(providers) { prov ->
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
                                        color = Color.White,
                                        modifier = Modifier.padding(top = 4.dp),
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    Button(
                        onClick = {
                            if (enMiLista) {
                                viewModel.quitarSerieDeMiLista(it)
                                favoritesViewModel.removeFavorite(userId, it.id.toString())
                            } else {
                                viewModel.agregarSerieAMiLista(it)
                                val favorito = Favoritos(
                                    id = it.id.toString(),
                                    titulo = it.name,                   // título
                                    posterUrl = "https://image.tmdb.org/t/p/w500${it.posterPath}", // url del póster
                                    tipo = "serie"
                                )
                                favoritesViewModel.addFavorite(userId, favorito)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = if (enMiLista) "Quitar de Mi Lista" else "Añadir a Mi Lista")
                    }
                }
            }

            // Comentarios Dialog
            if (showComentariosDialog) {
                AlertDialog(
                    onDismissRequest = { showComentariosDialog = false },
                    title = { Text("Comentarios", fontWeight = FontWeight.Bold) },
                    text = {
                        val listaComentarios = viewModel.comentarios[it.id] ?: emptyList()
                        if (listaComentarios.isEmpty()) {
                            Text("No hay comentarios aún.")
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                listaComentarios.forEach { comentario ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF2C2C2C))
                                            .padding(8.dp)
                                    ) {
                                        Text("Usuario: ${comentario.usuario}", color = Color.White)
                                        Text("Puntuación: ${comentario.puntuacion}/10", color = Color.Yellow)
                                        Text("Fecha: ${comentario.fecha}", color = Color.Gray, fontSize = 12.sp)
                                        Text(comentario.texto, color = Color.White)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showComentariosDialog = false }) {
                            Text("Cerrar")
                        }
                    }
                )
            }

            // Nuevo Comentario Dialog
            if (showNuevoComentarioDialog) {
                var usuario by remember { mutableStateOf("") }
                var comentarioTexto by remember { mutableStateOf("") }
                var puntuacion by remember { mutableStateOf(5) }
                var errorUsuario by remember { mutableStateOf(false) }
                var errorComentario by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { showNuevoComentarioDialog = false },
                    title = { Text("Nuevo Comentario") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = usuario,
                                onValueChange = {
                                    usuario = it
                                    errorUsuario = false
                                },
                                label = { Text("Nombre de usuario") },
                                isError = errorUsuario
                            )
                            Text("Puntuación:")
                            RatingBar(currentRating = puntuacion, onRatingSelected = { puntuacion = it })

                            OutlinedTextField(
                                value = comentarioTexto,
                                onValueChange = {
                                    comentarioTexto = it
                                    errorComentario = false
                                },
                                label = { Text("Comentario") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = errorComentario,
                                maxLines = 3
                            )
                            if (errorUsuario || errorComentario) {
                                Text("Por favor, rellena todos los campos", color = Color.Red, fontSize = 12.sp)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (usuario.isBlank()) errorUsuario = true
                            if (comentarioTexto.isBlank()) errorComentario = true
                            if (!errorUsuario && !errorComentario) {
                                val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                val fecha = java.time.LocalDate.now().format(formatter)

                                val nuevoComentario = Comentario(
                                    usuario = usuario.trim(),
                                    puntuacion = puntuacion,
                                    fecha = fecha,
                                    texto = comentarioTexto.trim()
                                )
                                it.id.let { id ->
                                    viewModel.agregarComentario(id, nuevoComentario)
                                    viewModel.cargarComentarios(id)
                                }
                                showNuevoComentarioDialog = false
                                usuario = ""
                                comentarioTexto = ""
                                puntuacion = 5
                            }
                        }) {
                            Text("Enviar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showNuevoComentarioDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Serie no encontrada", color = Color.White)
            }
        }
    }
}
