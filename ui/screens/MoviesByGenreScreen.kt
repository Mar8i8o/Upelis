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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import com.example.upelis_mariomarin.MoviesViewModel
import com.example.upelis_mariomarin.viewmodel.PlaylistsViewModel
import com.example.upelis_mariomarin.viewmodel.WatchedMoviesViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesByGenreScreen(
    genreId: Int,
    genreName: String,
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
    moviesViewModel: MoviesViewModel,
    playlistsViewModel: PlaylistsViewModel = viewModel(),
    watchedMoviesViewModel: WatchedMoviesViewModel = viewModel()
) {
    val genreMoviesMap by moviesViewModel.genreMoviesMap.collectAsState()
    val playlists by playlistsViewModel.playlists.collectAsState(initial = emptyList())
    val watchedMovies by watchedMoviesViewModel.watchedMovies.collectAsState()

    LaunchedEffect(genreId) {
        moviesViewModel.loadMoviesByGenre(genreId)
        watchedMoviesViewModel.loadAllWatchedMovies()
    }

    val moviesForGenre = genreMoviesMap[genreId] ?: emptyList()

    val favoriteMovieIds = remember(playlists) {
        playlists.flatMap { it.movieIds }.toSet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.offset(y = (-50).dp),
                title = { Text(genreName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        val adjustedPadding = PaddingValues(
            top = paddingValues.calculateTopPadding() - 40.dp,
            start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
            end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
            bottom = paddingValues.calculateBottomPadding()
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(adjustedPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(moviesForGenre) { movie ->
                val isWatched = watchedMovies[movie.id] == true

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                            if (isWatched) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Película vista",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
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

