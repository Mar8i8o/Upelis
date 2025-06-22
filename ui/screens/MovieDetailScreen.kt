package com.example.upelis_mariomarin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.upelis_mariomarin.data.model.MovieDetails
import com.example.upelis_mariomarin.data.model.Playlist
import com.example.upelis_mariomarin.viewmodel.PlaylistsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.ui.graphics.Color
import com.example.upelis_mariomarin.viewmodel.WatchedMoviesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieDetails: MovieDetails,
    onBack: () -> Unit,
    playlistsViewModel: PlaylistsViewModel = viewModel(),
    watchedMoviesViewModel: WatchedMoviesViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var isWatched by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(movieDetails.id) {
        loading = true
        isWatched = watchedMoviesViewModel.checkIfWatched(movieDetails.id)
        loading = false
    }

    val posterUrl = "https://image.tmdb.org/t/p/w500${movieDetails.posterPath}"

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {
            Box {
                AsyncImage(
                    model = posterUrl,
                    contentDescription = movieDetails.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp),
                    contentScale = ContentScale.Crop
                )

                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                        .background(Color.Black.copy(alpha = 0.6f), shape = MaterialTheme.shapes.small)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF210F37), shape = MaterialTheme.shapes.large)
                    .padding(24.dp)
            ) {
                Text(
                    text = movieDetails.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (!loading && isWatched) {
                    Text(
                        text = "✅ Ya has visto esta película",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = movieDetails.overview ?: "Sin descripción disponible",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "🎬 Duración: ${movieDetails.runtime ?: "?"} min",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "🎭 Géneros: ${movieDetails.genres?.joinToString { it.name } ?: "Desconocido"}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "⭐ Puntuación: ${movieDetails.voteAverage ?: "?"}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "📅 Estreno: ${movieDetails.releaseDate ?: "Desconocido"}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { showDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Añadir a playlist")
                    }

                    Button(
                        onClick = {
                            watchedMoviesViewModel.toggleWatched(movieDetails.id)
                            isWatched = !isWatched
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isWatched) Color(0xFF9E9E9E) else Color(0xFF2196F3),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (isWatched) "No vista" else "Vista")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showDialog) {
            AddToPlaylistDialog(
                movieId = movieDetails.id,
                onDismiss = { showDialog = false },
                playlistsViewModel = playlistsViewModel
            )
        }
    }
}

@Composable
fun AddToPlaylistDialog(
    movieId: Int,
    onDismiss: () -> Unit,
    playlistsViewModel: PlaylistsViewModel
) {
    val playlists by playlistsViewModel.playlists.collectAsState()
    var newPlaylistName by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val initiallySelected = playlists.filter { it.movieIds.contains(movieId) }.map { it.id }.toSet()
    var selectedPlaylists by remember { mutableStateOf(initiallySelected) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir a playlist") },
        text = {
            Column {
                if (playlists.isEmpty()) {
                    Text("No tienes playlists creadas. Crea una nueva:")
                } else {
                    Text("Selecciona las playlists donde quieres añadir/quitar esta película:")
                    Spacer(modifier = Modifier.height(8.dp))

                    playlists.forEach { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedPlaylists = if (selectedPlaylists.contains(playlist.id)) {
                                        selectedPlaylists - playlist.id
                                    } else {
                                        selectedPlaylists + playlist.id
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedPlaylists.contains(playlist.id),
                                onCheckedChange = { checked ->
                                    selectedPlaylists = if (checked) {
                                        selectedPlaylists + playlist.id
                                    } else {
                                        selectedPlaylists - playlist.id
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(playlist.name)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("O crea una nueva playlist:")
                }

                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Nombre playlist") },
                    isError = errorMsg != null
                )
                errorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            // Vacío aquí, botones los ponemos en dismissButton para centrarlos juntos
        },
        dismissButton = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        if (newPlaylistName.isBlank() && selectedPlaylists.isEmpty()) {
                            errorMsg = "Debes seleccionar o crear una playlist"
                            return@Button
                        }

                        errorMsg = null
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@Button
                        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId).child("playlists")

                        if (newPlaylistName.isNotBlank()) {
                            val newKey = db.push().key ?: return@Button
                            val newPlaylist = Playlist(
                                id = newKey,
                                name = newPlaylistName,
                                movieIds = listOf(movieId)
                            )
                            db.child(newKey).setValue(newPlaylist)
                                .addOnSuccessListener { onDismiss() }
                                .addOnFailureListener { errorMsg = "Error al guardar: ${it.message}" }
                        }

                        playlists.forEach { playlist ->
                            val playlistRef = db.child(playlist.id)
                            val containsMovie = playlist.movieIds.contains(movieId)
                            val shouldContainMovie = selectedPlaylists.contains(playlist.id)

                            if (shouldContainMovie && !containsMovie) {
                                val updatedMovieIds = playlist.movieIds.toMutableList()
                                updatedMovieIds.add(movieId)
                                playlistRef.child("movieIds").setValue(updatedMovieIds)
                                    .addOnFailureListener { errorMsg = "Error al actualizar: ${it.message}" }
                            } else if (!shouldContainMovie && containsMovie) {
                                val updatedMovieIds = playlist.movieIds.toMutableList()
                                updatedMovieIds.remove(movieId)
                                playlistRef.child("movieIds").setValue(updatedMovieIds)
                                    .addOnFailureListener { errorMsg = "Error al actualizar: ${it.message}" }
                            }
                        }

                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                ) {
                    Text("Guardar")
                }
            }
        }
    )
}
