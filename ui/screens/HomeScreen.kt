package com.example.upelis_mariomarin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import coil.compose.AsyncImage
import com.example.upelis_mariomarin.MoviesViewModel
import com.example.upelis_mariomarin.viewmodel.AuthViewModel
import com.example.upelis_mariomarin.viewmodel.PlaylistsViewModel
import com.example.upelis_mariomarin.viewmodel.WatchedMoviesViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    moviesViewModel: MoviesViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    playlistsViewModel: PlaylistsViewModel = viewModel(),
    watchedMoviesViewModel: WatchedMoviesViewModel = viewModel(), // añadido
    onLogout: () -> Unit = {},
    onMovieClick: (Int) -> Unit,
    onGenreClick: (Int, String) -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUserAuthenticated by authViewModel.isUserAuthenticated.collectAsState()
    val username by authViewModel.username.collectAsState()
    val profilePhotoUrl by authViewModel.profilePhotoUrl.collectAsState()
    val genres by moviesViewModel.genres.collectAsState(initial = emptyList())
    val genreMoviesMap by moviesViewModel.genreMoviesMap.collectAsState(initial = emptyMap())
    val playlists by playlistsViewModel.playlists.collectAsState(initial = emptyList())
    val watchedMovies by watchedMoviesViewModel.watchedMovies.collectAsState() // añadido

    val favoriteMovieIds = remember(playlists) {
        playlists.flatMap { it.movieIds }.toSet()
    }

    LaunchedEffect(Unit) {
        moviesViewModel.loadGenresAndMovies()
        authViewModel.loadUsername()
        authViewModel.loadUserProfilePhoto()
        watchedMoviesViewModel.loadAllWatchedMovies() // cargar vistas
    }

    LaunchedEffect(isUserAuthenticated) {
        if (!isUserAuthenticated) onLogout()
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hola, $username 👋",
                style = MaterialTheme.typography.titleLarge
            )

            if (!profilePhotoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = profilePhotoUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onProfileClick() },
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onProfileClick() }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "¡Elige tus películas y series favoritas!",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Yellow
        )

        /*
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { authViewModel.logout() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }

*/
        Spacer(modifier = Modifier.height(16.dp))



        if (genres.isEmpty()) {
            Text("No se encontraron géneros.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                items(genres) { genre ->
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onGenreClick(genre.id, genre.name) },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = genre.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = "Ver todo >", style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val movies = genreMoviesMap[genre.id] ?: emptyList()

                        if (movies.isEmpty()) {
                            Text(
                                text = "No hay películas disponibles",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(movies) { movie ->
                                    val isWatched = watchedMovies[movie.id] == true // check vista

                                    Column(
                                        modifier = Modifier
                                            .width(120.dp)
                                            .clickable { onMovieClick(movie.id) }
                                    ) {
                                        AsyncImage(
                                            model = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
                                            contentDescription = movie.title,
                                            modifier = Modifier
                                                .height(180.dp)
                                                .fillMaxWidth(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = movie.title ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 2,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (favoriteMovieIds.contains(movie.id)) {
                                                Icon(
                                                    imageVector = Icons.Filled.Star,
                                                    contentDescription = "Favorita",
                                                    tint = Color.Yellow,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            if (isWatched) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = "Película vista",
                                                    tint = Color(0xFF4CAF50),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
