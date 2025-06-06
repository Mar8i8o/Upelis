package com.example.upelis_mariomarin.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.upelis_mariomarin.viewmodel.PlaylistsViewModel
import com.example.upelis_mariomarin.viewmodel.MoviesViewModel
import androidx.navigation.NavController

@Composable
fun FavsScreen(
    navController: NavController,
    playlistsViewModel: PlaylistsViewModel = viewModel(),
    moviesViewModel: MoviesViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val playlists = playlistsViewModel.playlists.collectAsState().value
    val allMovies = moviesViewModel.movies.collectAsState().value

    Column(modifier = modifier.padding(16.dp)) {
        if (playlists.isEmpty()) {
            Text("No tienes playlists creadas.")
        } else {
            playlists.forEach { playlist ->
                Text(text = playlist.name, modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clickable {
                        navController.navigate("playlist/${playlist.id}")
                    })

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    val playlistMovies = allMovies.filter { it.id in playlist.movieIds }

                    items(playlistMovies) { movie ->
                        Card(
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
}
