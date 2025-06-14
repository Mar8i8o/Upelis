package com.example.upelis_mariomarin.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.upelis_mariomarin.viewmodel.PlaylistsViewModel
import com.example.upelis_mariomarin.MoviesViewModel
import com.example.upelis_mariomarin.viewmodel.AuthViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavsScreen(
    navController: NavController,
    playlistsViewModel: PlaylistsViewModel = viewModel(),
    moviesViewModel: MoviesViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val ownPlaylists = playlistsViewModel.playlists.collectAsState().value
    val sharedPlaylists = playlistsViewModel.sharedPlaylists.collectAsState().value
    val allMovies = moviesViewModel.movies.collectAsState().value
    val friendsList by authViewModel.friendsList.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    var showShareDialog by remember { mutableStateOf(false) }
    var selectedPlaylistId by remember { mutableStateOf<String?>(null) }

    // Estado para la pestaña seleccionada: 0 = Mis playlists, 1 = Compartidas
    var selectedTabIndex by remember { mutableStateOf(0) }

    // Iniciar listener para playlists compartidas solo una vez
    LaunchedEffect(Unit) {
        playlistsViewModel.startListeningSharedPlaylists()
    }

    Column(modifier = modifier.padding(16.dp)) {
        val tabs = listOf("Mis playlists", "Compartidas")

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTabIndex == 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        showCreateDialog = true
                        errorMsg = null
                        newPlaylistName = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFEB3B),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Añadir Playlist")
                }
            }
        }

        val playlistsToShow = if (selectedTabIndex == 0) ownPlaylists else sharedPlaylists

        if (playlistsToShow.isEmpty()) {
            Text(if (selectedTabIndex == 0) "No tienes playlists creadas." else "No tienes playlists compartidas.")
        } else {
            playlistsToShow.forEach { playlist ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.clickable {
                            navController.navigate("playlist/${playlist.id}")
                        }
                    )

                    if (selectedTabIndex == 0) {
                        IconButton(
                            onClick = {
                                selectedPlaylistId = playlist.id
                                authViewModel.loadFriends()
                                showShareDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartir playlist",
                                tint = Color(0xFF03A9F4)
                            )
                        }
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    val playlistMovies = allMovies.filter { it.id in playlist.movieIds }

                    items(playlistMovies) { movie ->
                        Column(
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            if (newPlaylistName.isBlank()) {
                                errorMsg = "El nombre no puede estar vacío"
                                return@Button
                            }
                            playlistsViewModel.createPlaylist(
                                name = newPlaylistName.trim(),
                                movieId = -1,
                                onSuccess = {
                                    showCreateDialog = false
                                    newPlaylistName = ""
                                    errorMsg = null
                                },
                                onError = { msg -> errorMsg = msg }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Aceptar")
                    }
                    Button(
                        onClick = {
                            showCreateDialog = false
                            errorMsg = null
                            newPlaylistName = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336),
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

    if (showShareDialog && selectedPlaylistId != null) {
        AlertDialog(
            onDismissRequest = {
                showShareDialog = false
                selectedPlaylistId = null
            },
            title = { Text("Compartir playlist") },
            text = {
                Column {
                    if (friendsList.isEmpty()) {
                        Text("No tienes amigos para compartir.")
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight(0.5f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            friendsList.forEach { friend ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            playlistsViewModel.sharePlaylistWithFriend(
                                                playlistId = selectedPlaylistId!!,
                                                friendUid = friend.uid
                                            )
                                            showShareDialog = false
                                            selectedPlaylistId = null
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
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(Color.LightGray)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(friend.username, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    showShareDialog = false
                    selectedPlaylistId = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
