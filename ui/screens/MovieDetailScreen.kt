package com.example.upelis_mariomarin.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieDetails: MovieDetails,
    onBack: () -> Unit,
    playlistsViewModel: PlaylistsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var isWatched by remember { mutableStateOf(false) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Cargar si la película ya ha sido vista
    LaunchedEffect(movieDetails.id) {
        val dbRef = FirebaseDatabase.getInstance().reference
            .child("users").child(userId ?: "").child("watchedMovies").child(movieDetails.id.toString())

        dbRef.get().addOnSuccessListener { snapshot ->
            isWatched = snapshot.getValue(Boolean::class.java) == true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(movieDetails.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                modifier = Modifier.offset(y = (-50).dp)
            )
        },
        contentWindowInsets = WindowInsets(0),
        modifier = modifier
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(
                    top = 80.dp,
                    bottom = innerPadding.calculateBottomPadding(),
                    start = 16.dp,
                    end = 16.dp
                )
                .verticalScroll(rememberScrollState())
        ) {
            val posterUrl = "https://image.tmdb.org/t/p/w500${movieDetails.posterPath}"

            AsyncImage(
                model = posterUrl,
                contentDescription = movieDetails.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isWatched) {
                Text(
                    text = "✅ Ya has visto esta película",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF4CAF50)),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = movieDetails.overview ?: "Sin descripción",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Duración: ${movieDetails.runtime ?: "Desconocida"} minutos",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Géneros: ${movieDetails.genres?.joinToString { it.name } ?: "No disponible"}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Puntuación: ${movieDetails.voteAverage ?: "No disponible"}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { showDialog = true }) {
                Text("Añadir a playlist")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val dbRef = FirebaseDatabase.getInstance().reference
                        .child("users").child(userId ?: return@Button).child("watchedMovies")
                        .child(movieDetails.id.toString())

                    if (isWatched) {
                        dbRef.removeValue()
                    } else {
                        dbRef.setValue(true)
                    }

                    isWatched = !isWatched
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isWatched) Color(0xFF9E9E9E) else Color(0xFF2196F3),
                    contentColor = Color.White
                )
            ) {
                Text(if (isWatched) "Marcar como no vista" else "Marcar como vista")
            }
        }
    }

    if (showDialog) {
        AddToPlaylistDialog(
            movieId = movieDetails.id,
            onDismiss = { showDialog = false },
            playlistsViewModel = playlistsViewModel
        )
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
                ),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF44336),
                    contentColor = Color.White
                )
            ) {
                Text("Cancelar")
            }
        }
    )
}
