package com.example.upelis_mariomarin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.upelis_mariomarin.MoviesViewModel
import com.example.upelis_mariomarin.viewmodel.AuthViewModel
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    moviesViewModel: MoviesViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onLogout: () -> Unit = {},
    onMovieClick: (Int) -> Unit,
    onGenreClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUserAuthenticated by authViewModel.isUserAuthenticated.collectAsState()
    val genres by moviesViewModel.genres.collectAsState(initial = emptyList())
    val genreMoviesMap by moviesViewModel.genreMoviesMap.collectAsState(initial = emptyMap())

    // Cargar solo una vez al entrar
    LaunchedEffect(Unit) {
        moviesViewModel.loadGenresAndMovies()
    }

    // Si no está autenticado, salir
    LaunchedEffect(isUserAuthenticated) {
        if (!isUserAuthenticated) onLogout()
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
                                        Text(
                                            text = movie.title ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 2
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
