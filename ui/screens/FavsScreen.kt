package com.example.upelis_mariomarin.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.upelis_mariomarin.viewmodel.PlaylistsViewModel
import com.example.upelis_mariomarin.viewmodel.MoviesViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavsScreen(
    navController: NavController,
    playlistsViewModel: PlaylistsViewModel = viewModel(),
    moviesViewModel: MoviesViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val playlists = playlistsViewModel.playlists.collectAsState().value
    val allMovies = moviesViewModel.movies.collectAsState().value

    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.padding(16.dp)) {
        // Botón para crear nueva playlist
        Button(onClick = {
            showCreateDialog = true
            errorMsg = null
            newPlaylistName = ""
        }, modifier = Modifier.padding(bottom = 16.dp)) {
            Text("Crear nueva playlist")
        }

        if (playlists.isEmpty()) {
            Text("No tienes playlists creadas.")
        } else {
            playlists.forEach { playlist ->
                Text(
                    text = playlist.name,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable {
                            navController.navigate("playlist/${playlist.id}")
                        }
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    val playlistMovies = allMovies.filter { it.id in playlist.movieIds }

                    items(playlistMovies) { movie ->
                        Card(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable {
                                    navController.navigate("movie_detail/${movie.id}")
                                }
                        ) {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
                                contentDescription = movie.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                errorMsg = null
                newPlaylistName = ""
            },
            title = { Text("Nueva Playlist") },
            text = {
                Column {
                    TextField(
                        value = newPlaylistName,
                        onValueChange = {
                            newPlaylistName = it
                            errorMsg = null
                        },
                        label = { Text("Nombre de la playlist") },
                        singleLine = true,
                        isError = errorMsg != null
                    )
                    if (errorMsg != null) {
                        Text(
                            text = errorMsg ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlaylistName.isBlank()) {
                            errorMsg = "El nombre no puede estar vacío"
                            return@TextButton
                        }
                        playlistsViewModel.createPlaylist(
                            name = newPlaylistName.trim(),
                            movieId = -1, // Sin película añadida al crear la playlist
                            onSuccess = {
                                showCreateDialog = false
                                newPlaylistName = ""
                                errorMsg = null
                            },
                            onError = { msg ->
                                errorMsg = msg
                            }
                        )
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreateDialog = false
                        errorMsg = null
                        newPlaylistName = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
