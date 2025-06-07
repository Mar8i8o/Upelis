package com.example.upelis_mariomarin.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.upelis_mariomarin.MoviesViewModel
import com.example.upelis_mariomarin.viewmodel.PlaylistsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayListScreen(
    playlistId: String,
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
    playlistsViewModel: PlaylistsViewModel = viewModel(),
    moviesViewModel: MoviesViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val playlists = playlistsViewModel.playlists.collectAsState().value
    val allMovies = moviesViewModel.movies.collectAsState().value

    val playlist = playlists.find { it.id == playlistId }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets(0))
                    .offset(y = (-5).dp)
                    .padding(vertical = 10.dp),
                title = { Text(text = playlist?.name ?: "Playlist") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (playlist != null) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar nombre playlist"
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar playlist"
                            )
                        }
                    }
                },
                windowInsets = WindowInsets(0)
            )
        },
        contentWindowInsets = WindowInsets(0),
        modifier = modifier
    ) { paddingValues ->

        if (playlist == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 0.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Playlist no encontrada")
            }
        } else {
            val playlistMovies = allMovies.filter { it.id in playlist.movieIds }
            LazyColumn(
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(),
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(playlistMovies) { movie ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMovieClick(movie.id) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
                                contentDescription = movie.title,
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(150.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.fillMaxHeight()) {
                                Text(
                                    text = movie.title ?: "Título desconocido",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo eliminar playlist
    if (showDeleteDialog && playlist != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar playlist") },
            text = {
                Text("¿Estás seguro que quieres eliminar la playlist \"${playlist.name}\"? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            playlistsViewModel.deletePlaylist(playlist.id)
                            showDeleteDialog = false
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50), // verde forzado
                            contentColor = Color.White
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Eliminar")
                    }
                    Button(
                        onClick = { showDeleteDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336), // rojo forzado
                            contentColor = Color.White
                        )
                    ) {
                        Text("Cancelar")
                    }
                }
            },
            dismissButton = {}
        )
    }

    // Diálogo editar nombre playlist
    if (showEditDialog && playlist != null) {
        LaunchedEffect(Unit) {
            newPlaylistName = playlist.name
        }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar nombre de la playlist") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            if (newPlaylistName.isNotBlank() && newPlaylistName != playlist.name) {
                                playlistsViewModel.renamePlaylist(playlist.id, newPlaylistName.trim())
                            }
                            showEditDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50), // verde forzado
                            contentColor = Color.White
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Aceptar")
                    }
                    Button(
                        onClick = { showEditDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336), // rojo forzado
                            contentColor = Color.White
                        )
                    ) {
                        Text("Cancelar")
                    }
                }
            },
            dismissButton = {}
        )
    }
}
