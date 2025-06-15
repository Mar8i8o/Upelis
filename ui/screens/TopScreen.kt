package com.example.upelis_mariomarin.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.upelis_mariomarin.MoviesViewModel
import com.example.upelis_mariomarin.viewmodel.AuthViewModel
import com.example.upelis_mariomarin.viewmodel.PlaylistsViewModel
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TopScreen(
    moviesViewModel: MoviesViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    playlistsViewModel: PlaylistsViewModel = viewModel(),
    onLogout: () -> Unit = {},
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusMessage by moviesViewModel.statusMessage.collectAsState()
    val movies by moviesViewModel.movies.collectAsState(initial = emptyList())
    val isUserAuthenticated by authViewModel.isUserAuthenticated.collectAsState()
    val playlists by playlistsViewModel.playlists.collectAsState(initial = emptyList())
    val movieDetailsMap by moviesViewModel.movieDetailsMap.collectAsState()

    LaunchedEffect(isUserAuthenticated) {
        if (!isUserAuthenticated) {
            onLogout()
        }
    }

    val favoriteMovieIds = remember(playlists) {
        playlists.flatMap { it.movieIds }.toSet()
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Button(
            onClick = { authViewModel.logout() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    text = statusMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            items(movies) { movie ->

                // Lanzar la carga del detalle solo si no está cargado ya
                LaunchedEffect(movie.id) {
                    if (movieDetailsMap[movie.id] == null) {
                        moviesViewModel.fetchMovieDetails(movie.id)
                    }
                }

                val details = movieDetailsMap[movie.id]
                val year = movie.releaseDate?.take(4) ?: "----"
                val duration = details?.runtime ?: 0

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMovieClick(movie.id) }
                ) {
                    Row(modifier = Modifier.padding(8.dp)) {
                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
                            contentDescription = movie.title,
                            modifier = Modifier
                                .width(100.dp)
                                .height(150.dp),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(end = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = movie.title ?: "Título desconocido",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                if (favoriteMovieIds.contains(movie.id)) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "Favorita",
                                        tint = Color.Yellow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Año: $year • Duración: ${if (duration > 0) "$duration min" else "Desconocida"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = movie.overview?.take(150)?.plus("...") ?: "Sin descripción.",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 4
                            )
                        }
                    }
                }
            }
        }
    }
}
