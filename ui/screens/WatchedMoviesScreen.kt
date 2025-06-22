package com.example.upelis_mariomarin.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.upelis_mariomarin.MoviesViewModel
import com.example.upelis_mariomarin.viewmodel.WatchedMoviesViewModel
import com.example.upelis_mariomarin.viewmodel.PlaylistsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchedMoviesScreen(
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
    moviesViewModel: MoviesViewModel = viewModel(),
    watchedMoviesViewModel: WatchedMoviesViewModel = viewModel(),
    playlistsViewModel: PlaylistsViewModel = viewModel()
) {
    val watchedMoviesMap by watchedMoviesViewModel.watchedMovies.collectAsState()
    val allMovies by moviesViewModel.movies.collectAsState()
    val playlists by playlistsViewModel.playlists.collectAsState()

    LaunchedEffect(Unit) {
        watchedMoviesViewModel.loadAllWatchedMovies()
        moviesViewModel.loadAllMovies()
    }

    val watchedMovieIds = watchedMoviesMap.filterValues { it }.keys
    val watchedMovies = allMovies.filter { it.id in watchedMovieIds }

    val favoriteMovieIds = remember(playlists) {
        playlists.flatMap { it.movieIds }.toSet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Películas Vistas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (watchedMovies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No has marcado ninguna película como vista.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(watchedMovies) { movie ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMovieClick(movie.id) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
                            contentDescription = movie.title,
                            modifier = Modifier.size(width = 80.dp, height = 120.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = movie.title ?: "",
                                    style = MaterialTheme.typography.titleMedium,
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
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Vista",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = movie.overview ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3
                            )
                        }
                    }
                }
            }
        }
    }
}
