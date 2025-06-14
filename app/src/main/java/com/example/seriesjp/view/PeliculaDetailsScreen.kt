// PeliculaDetailsScreen.kt
package com.example.seriesjp.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.seriesjp.model.Comentario
import com.example.seriesjp.model.Peliculas
import com.example.seriesjp.model.Favoritos
import com.example.seriesjp.viewmodel.PeliculasViewModel
import com.example.seriesjp.viewmodel.FavoritesViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeliculaDetailsScreen(
    navController: NavHostController,
    peliculaId: Int?,
    viewModel: PeliculasViewModel,
    favoritesViewModel: FavoritesViewModel,
    userId: String
) {
    // Observamos listas y datos del ViewModel de películas
    val populares by viewModel.peliculasList
    val recomendadas by viewModel.recommendedPeliculas

    // Buscamos la película en populares o recomendadas
    val pelicula: Peliculas? = peliculaId?.let { id ->
        populares.find { it.id == id } ?: recomendadas.find { it.id == id }
    }

    // Al iniciar o cambiar peliculaId, cargamos datos
    LaunchedEffect(peliculaId) {
        peliculaId?.let {
            viewModel.loadWatchProviders(it)
            viewModel.cargarPuntuacionesDesdeFirestore(userId)
            viewModel.cargarMiListaDesdeFirestore(userId)
            viewModel.cargarComentariosDesdeFirestore(it)
            // Cargar también la lista de favoritos actuales
            favoritesViewModel.loadFavorites(userId)
        }
    }

    // Observamos proveedores, puntuaciones, comentarios
    val providers by viewModel.watchProviders
    val ratings by viewModel.ratings
    val comentarios by viewModel.comentarios

    // Estado local para la puntuación del usuario en esta pantalla
    var userRating by remember { mutableStateOf(ratings[peliculaId] ?: 0) }
    var showComentariosDialog by remember { mutableStateOf(false) }
    var showNuevoComentarioDialog by remember { mutableStateOf(false) }

    // Estados de formulario nuevo comentario
    var usuario by remember { mutableStateOf("") }
    var comentarioTexto by remember { mutableStateOf("") }
    var puntuacion by remember { mutableStateOf(5) }

    // Observamos lista de favoritos para este usuario
    val favoritosList by favoritesViewModel.favorites.collectAsState()

    // Composable reutilizable RatingBar
    @Composable
    fun RatingBar(
        currentRating: Int,
        onRatingSelected: (Int) -> Unit,
        modifier: Modifier = Modifier,
        maxRating: Int = 10,
        iconSize: Int = 24
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

    // Back button seguro
    @Composable
    fun BackButton() {
        IconButton(onClick = {
            val popped = navController.popBackStack()
            if (!popped) {
                // Ajusta la ruta principal según tu NavGraph
                navController.navigate("home/$userId") {
                    popUpTo("home/$userId") { inclusive = true }
                }
            }
        }) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
        }
    }

    pelicula?.let { p ->
        // Comprobar si esta en favoritos: comparamos id string
        val enFavoritos = favoritosList.any { it.id == p.id.toString() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF4C6D7), Color(0xFF121212))
                    )
                )
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                BackButton()
            }

            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${p.posterPath}",
                contentDescription = p.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Crop
            )

            Text(
                text = p.title ?: "",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Descripción:", fontWeight = FontWeight.Bold, color = Color.White)
                Text(p.overview ?: "", color = Color.White)
                Spacer(Modifier.height(8.dp))

                Text("Fecha de estreno: ${p.releaseDate}", color = Color.White)
                Spacer(Modifier.height(8.dp))

                Text("Puntuación media: ${p.voteAverage}", color = Color.White)
                Spacer(Modifier.height(8.dp))

                Text("Tu puntuación:", fontWeight = FontWeight.Bold, color = Color.White)
                RatingBar(
                    currentRating = userRating,
                    maxRating = 10,
                    iconSize = 24,
                    onRatingSelected = {
                        userRating = it
                        peliculaId?.let { id -> viewModel.guardarPuntuacion(userId, id, it) }
                    }
                )

                Spacer(Modifier.height(16.dp))

                if (providers.isNotEmpty()) {
                    Text(
                        "Dónde verla en España:",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        items(providers) { prov ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(80.dp)
                            ) {
                                AsyncImage(
                                    model = "https://image.tmdb.org/t/p/w92${prov.logoPath}",
                                    contentDescription = prov.providerName,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    prov.providerName,
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (enFavoritos) {
                            favoritesViewModel.removeFavorite(userId, p.id.toString())
                        } else {
                            val nuevoFav = Favoritos(
                                id = p.id.toString(),
                                titulo = p.title ?: "",
                                posterUrl = "https://image.tmdb.org/t/p/w500${p.posterPath}",
                                tipo = "pelicula"
                            )
                            favoritesViewModel.addFavorite(userId, nuevoFav)
                        }
                        // Tras la acción, recargar lista para actualizar UI
                        favoritesViewModel.loadFavorites(userId)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (enFavoritos) "Quitar de Favoritos" else "Añadir a Favoritos")
                }

                Spacer(Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = { showComentariosDialog = true }) {
                        Text("Comentarios")
                    }
                    Button(onClick = { showNuevoComentarioDialog = true }) {
                        Text("Nuevo Comentario")
                    }
                }
            }
        }

        // Diálogo de comentarios existentes
        if (showComentariosDialog) {
            AlertDialog(
                onDismissRequest = { showComentariosDialog = false },
                title = { Text("Comentarios") },
                text = {
                    val listaComentarios = comentarios[p.id] ?: emptyList()
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

        // Diálogo de nuevo comentario
        if (showNuevoComentarioDialog) {
            AlertDialog(
                onDismissRequest = { showNuevoComentarioDialog = false },
                title = { Text("Nuevo Comentario") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = usuario,
                            onValueChange = { usuario = it },
                            label = { Text("Nombre de usuario") },
                            singleLine = true
                        )
                        Text("Puntuación:")
                        RatingBar(
                            currentRating = puntuacion,
                            onRatingSelected = { puntuacion = it }
                        )
                        OutlinedTextField(
                            value = comentarioTexto,
                            onValueChange = { comentarioTexto = it },
                            label = { Text("Comentario") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (usuario.isBlank() || comentarioTexto.isBlank()) return@TextButton
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        val fecha = LocalDate.now().format(formatter)
                        val nuevoComentario = Comentario(
                            usuario = usuario.trim(),
                            puntuacion = puntuacion,
                            fecha = fecha,
                            texto = comentarioTexto.trim()
                        )
                        peliculaId?.let { viewModel.agregarComentario(it, nuevoComentario) }
                        viewModel.cargarComentariosDesdeFirestore(peliculaId!!)
                        // Limpiar formulario
                        usuario = ""
                        comentarioTexto = ""
                        puntuacion = 5
                        showNuevoComentarioDialog = false
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
        // Si no existe la película
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Película no encontrada", color = Color.White)
        }
    }
}
