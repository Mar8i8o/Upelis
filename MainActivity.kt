package com.example.upelis_mariomarin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.upelis_mariomarin.ui.navigation.BottomNavigationBar
import com.example.upelis_mariomarin.ui.screens.*
import com.example.upelis_mariomarin.ui.theme.UPelis_MarioMarinTheme
import com.example.upelis_mariomarin.viewmodel.AuthViewModel
import com.example.upelis_mariomarin.MoviesViewModel
import com.example.upelis_mariomarin.viewmodel.PlaylistsViewModel

class MainActivity : ComponentActivity() {

    private val moviesViewModel by viewModels<MoviesViewModel>()
    private val authViewModel by viewModels<AuthViewModel>()
    private val playlistsViewModel by viewModels<PlaylistsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            UPelis_MarioMarinTheme {
                var isLoggedIn by remember { mutableStateOf(authViewModel.currentUser != null) }
                var showRegister by remember { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when {
                        !isLoggedIn && !showRegister -> {
                            LoginScreen(
                                authViewModel = authViewModel,
                                modifier = Modifier.padding(innerPadding),
                                onLoginSuccess = { isLoggedIn = true },
                                onNavigateToRegister = { showRegister = true }
                            )
                        }

                        !isLoggedIn && showRegister -> {
                            RegisterScreen(
                                authViewModel = authViewModel,
                                modifier = Modifier.padding(innerPadding),
                                onRegisterSuccess = { isLoggedIn = true },
                                onNavigateToLogin = { showRegister = false }
                            )
                        }

                        else -> {
                            val navController = rememberNavController()

                            Scaffold(
                                bottomBar = {
                                    BottomNavigationBar(navController = navController)
                                },
                                modifier = Modifier.padding(innerPadding)
                            ) {
                                NavHost(
                                    navController = navController,
                                    startDestination = "home",
                                    modifier = Modifier.padding(it)
                                ) {
                                    composable("home") {
                                        HomeScreen(
                                            moviesViewModel = moviesViewModel,
                                            authViewModel = authViewModel,
                                            playlistsViewModel = playlistsViewModel,
                                            onLogout = {
                                                isLoggedIn = false
                                                showRegister = false
                                            },
                                            onMovieClick = { movieId ->
                                                navController.navigate("movie_detail/$movieId")
                                            },
                                            onGenreClick = { genreId, genreName ->
                                                navController.navigate("movies_by_genre/$genreId/$genreName")
                                            },
                                            onProfileClick = {
                                                navController.navigate("user_profile")
                                            }
                                        )
                                    }

                                    composable("search") {
                                        SearchScreen(
                                            moviesViewModel = moviesViewModel,
                                            authViewModel = authViewModel,
                                            onMovieClick = { movieId ->
                                                navController.navigate("movie_detail/$movieId")
                                            },
                                            onGenreClick = { genreId, genreName ->
                                                navController.navigate("movies_by_genre/$genreId/$genreName")
                                            }
                                        )
                                    }

                                    composable("top") {
                                        TopScreen(
                                            moviesViewModel = moviesViewModel,
                                            onMovieClick = { movieId ->
                                                navController.navigate("movie_detail/$movieId")
                                            }
                                        )
                                    }

                                    composable("favs") {
                                        FavsScreen(
                                            navController = navController,
                                            playlistsViewModel = playlistsViewModel,
                                            moviesViewModel = moviesViewModel
                                        )
                                    }

                                    composable(
                                        "movie_detail/{movieId}",
                                        arguments = listOf(navArgument("movieId") { type = NavType.IntType })
                                    ) { backStackEntry ->
                                        val movieId = backStackEntry.arguments?.getInt("movieId") ?: 0

                                        LaunchedEffect(movieId) {
                                            moviesViewModel.fetchMovieDetails(movieId)
                                        }

                                        val movieDetails by moviesViewModel.selectedMovieDetails.collectAsState()

                                        if (movieDetails != null) {
                                            MovieDetailScreen(
                                                movieDetails = movieDetails!!,
                                                onBack = {
                                                    moviesViewModel.clearMovieDetails()
                                                    navController.popBackStack()
                                                }
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("Cargando detalles...")
                                            }
                                        }
                                    }

                                    composable(
                                        "playlist/{playlistId}",
                                        arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
                                    ) { backStackEntry ->
                                        val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""

                                        PlayListScreen(
                                            playlistId = playlistId,
                                            onBack = { navController.popBackStack() },
                                            onMovieClick = { movieId ->
                                                navController.navigate("movie_detail/$movieId")
                                            },
                                            playlistsViewModel = playlistsViewModel,
                                            moviesViewModel = moviesViewModel
                                        )
                                    }

                                    composable(
                                        route = "movies_by_genre/{genreId}/{genreName}",
                                        arguments = listOf(
                                            navArgument("genreId") { type = NavType.IntType },
                                            navArgument("genreName") { type = NavType.StringType }
                                        )
                                    ) { backStackEntry ->
                                        val genreId = backStackEntry.arguments?.getInt("genreId") ?: 0
                                        val genreName = backStackEntry.arguments?.getString("genreName") ?: ""

                                        MoviesByGenreScreen(
                                            genreId = genreId,
                                            genreName = genreName,
                                            onBack = { navController.popBackStack() },
                                            onMovieClick = { movieId ->
                                                navController.navigate("movie_detail/$movieId")
                                            },
                                            moviesViewModel = moviesViewModel
                                        )
                                    }

                                    // MODIFICADO: Perfil de usuario con acceso a películas vistas
                                    composable("user_profile") {
                                        UserScreen(
                                            authViewModel = authViewModel,
                                            onBack = { navController.popBackStack() },
                                            onGoToMoviesScreen = {
                                                navController.navigate("watched_movies")
                                            }
                                        )
                                    }

                                    composable("watched_movies") {
                                        WatchedMoviesScreen(
                                            moviesViewModel = moviesViewModel,
                                            onBack = { navController.popBackStack() },
                                            onMovieClick = { movieId ->
                                                navController.navigate("movie_detail/$movieId")
                                            }
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
