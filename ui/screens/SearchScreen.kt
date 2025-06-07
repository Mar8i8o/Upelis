package com.example.upelis_mariomarin.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.upelis_mariomarin.MoviesViewModel
import com.example.upelis_mariomarin.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    moviesViewModel: MoviesViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onLogout: () -> Unit = {},
    onMovieClick: (Int) -> Unit,
    onGenreClick: (Int, String) -> Unit, // lo añadí para poder usar el onGenreClick igual que en HomeScreen
    modifier: Modifier = Modifier
) {
    val isUserAuthenticated by authViewModel.isUserAuthenticated.collectAsState()
    val genres by moviesViewModel.genres.collectAsState(initial = emptyList())
    val genreMoviesMap by moviesViewModel.genreMoviesMap.collectAsState(initial = emptyMap())

    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(isUserAuthenticated) {
        if (!isUserAuthenticated) onLogout()
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        // Aquí quité el botón cerrar sesión para que no se vea, pero si quieres lo pones igual que HomeScreen

        Spacer(modifier = Modifier.height(16.dp))

        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar películas...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            singleLine = true,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (searchQuery.text.isBlank()) {
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
        } else {
            // Mostrar resultado de búsqueda en grid 4x por fila
            val allMovies = genreMoviesMap.values.flatten()
            val filteredMovies = remember(searchQuery.text, allMovies) {
                allMovies.filter { movie ->
                    movie.title?.contains(searchQuery.text, ignoreCase = true) == true
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredMovies) { movie ->
                    Column(
                        modifier = Modifier
                            .clickable { onMovieClick(movie.id) }
                            .fillMaxWidth()
                    ) {
                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
                            contentDescription = movie.title,
                            modifier = Modifier
                                .aspectRatio(2 / 3f),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = movie.title ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

