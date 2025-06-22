package com.example.upelis_mariomarin.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.LayoutDirection


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

    var searchQuery by remember { mutableStateOf("") }

    val filteredMovies = if (searchQuery.isBlank()) {
        watchedMovies
    } else {
        watchedMovies.filter {
            it.title?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.offset(y = (-50).dp),
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar películas...") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Buscar"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors()
            )
        }
    ) { paddingValues ->
        val adjustedPadding = PaddingValues(
            top = paddingValues.calculateTopPadding() - 40.dp,
            start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
            end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
            bottom = paddingValues.calculateBottomPadding()
        )

        if (filteredMovies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(adjustedPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No se encontraron películas.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(adjustedPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(filteredMovies) { movie ->
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

