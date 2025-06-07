package com.example.upelis_mariomarin.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.upelis_mariomarin.MoviesViewModel
import com.example.upelis_mariomarin.viewmodel.AuthViewModel
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SearchScreen(
    moviesViewModel: MoviesViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onLogout: () -> Unit = {},
    onMovieClick: (Int) -> Unit,  // <-- Aquí
    modifier: Modifier = Modifier
) {
    val statusMessage by moviesViewModel.statusMessage.collectAsState()
    val movies by moviesViewModel.movies.collectAsState(initial = emptyList())
    val isUserAuthenticated by authViewModel.isUserAuthenticated.collectAsState()

    LaunchedEffect(isUserAuthenticated) {
        if (!isUserAuthenticated) {
            onLogout()
        }
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onMovieClick(movie.id) }  // <-- Aquí para llamar la función con id
                        .padding(8.dp)
                ) {
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
                        contentDescription = movie.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = movie.title ?: "Título desconocido")
                }
            }
        }
    }
}
