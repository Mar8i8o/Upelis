package com.example.upelis_mariomarin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.upelis_mariomarin.MoviesViewModel
import com.example.upelis_mariomarin.viewmodel.AuthViewModel
import com.example.upelis_mariomarin.viewmodel.PlaylistsViewModel
import com.example.upelis_mariomarin.viewmodel.WatchedMoviesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayListScreen(
    playlistId: String,
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
    playlistsViewModel: PlaylistsViewModel = viewModel(),
    moviesViewModel: MoviesViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    watchedMoviesViewModel: WatchedMoviesViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val playlists by playlistsViewModel.playlists.collectAsState()
    val allMovies by moviesViewModel.movies.collectAsState()
    val movieDetailsMap by moviesViewModel.movieDetailsMap.collectAsState()
    val friendsList by authViewModel.friendsList.collectAsState()
    val watchedMovies by watchedMoviesViewModel.watchedMovies.collectAsState()

    val playlist = playlists.find { it.id == playlistId }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var showShareDialog by remember { mutableStateOf(false) }

    var movieToRemoveId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(playlist) {
        playlist?.movieIds?.forEach { id ->
            if (allMovies.none { it.id == id }) {
                moviesViewModel.loadMovieIfMissing(id)
            }
            if (movieDetailsMap[id] == null) {
                moviesViewModel.fetchMovieDetails(id)
            }
        }
        watchedMoviesViewModel.loadAllWatchedMovies()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = playlist?.name ?: "Playlist") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (playlist != null) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar nombre playlist")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar playlist")
                        }
                        IconButton(onClick = {
                            authViewModel.loadFriends()
                            showShareDialog = true
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir playlist")
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->

        if (playlist == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Playlist no encontrada")
            }
        } else {
            val playlistMovies = allMovies.filter { it.id in playlist.movieIds }

            LazyColumn(
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                items(playlistMovies) { movie ->
                    val details = movieDetailsMap[movie.id]
                    val year = movie.releaseDate?.take(4) ?: "----"
                    val duration = details?.runtime ?: 0
                    val isWatched = watchedMovies[movie.id] == true

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMovieClick(movie.id) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
                            contentDescription = movie.title,
                            modifier = Modifier
                                .width(100.dp)
                                .height(150.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = movie.title ?: "Título desconocido",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Año: $year • Duración: ${if (duration > 0) "$duration min" else "Desconocida"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            if (isWatched) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Vista",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = movie.overview?.take(150)?.plus("...") ?: "Sin descripción.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        IconButton(onClick = {
                            movieToRemoveId = movie.id
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar película de playlist")
                        }
                    }
                }
            }
        }
    }

    // Dialogo eliminar playlist completa
    if (showDeleteDialog && playlist != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar playlist") },
            text = { Text("¿Seguro que deseas eliminar \"${playlist.name}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(onClick = {
                    playlistsViewModel.deletePlaylist(playlist.id)
                    showDeleteDialog = false
                    onBack()
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialogo eliminar película de la playlist
    if (movieToRemoveId != null && playlist != null) {
        AlertDialog(
            onDismissRequest = { movieToRemoveId = null },
            title = { Text("Eliminar película") },
            text = { Text("¿Quieres eliminar esta película de la playlist \"${playlist.name}\"?") },
            confirmButton = {
                Button(onClick = {
                    playlistsViewModel.removeMovieFromPlaylist(
                        playlistId = playlist.id,
                        movieId = movieToRemoveId!!,
                        onSuccess = { movieToRemoveId = null },
                        onError = { movieToRemoveId = null } // Muestra un Toast si quieres
                    )
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { movieToRemoveId = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialogo editar nombre
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
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newPlaylistName.isNotBlank()) {
                        playlistsViewModel.renamePlaylist(playlist.id, newPlaylistName.trim())
                    }
                    showEditDialog = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialogo compartir
    if (showShareDialog && playlist != null) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Compartir playlist") },
            text = {
                if (friendsList.isEmpty()) {
                    Text("No tienes amigos para compartir.")
                } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        friendsList.forEach { friend ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        playlistsViewModel.sharePlaylistWithFriend(playlist.id, friend.uid)
                                        showShareDialog = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!friend.profilePhotoUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = friend.profilePhotoUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(MaterialTheme.shapes.small)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color.LightGray, MaterialTheme.shapes.small)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(friend.username)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

