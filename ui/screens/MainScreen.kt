package com.example.upelis_mariomarin.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import com.example.upelis_mariomarin.viewmodel.AuthViewModel
import com.example.upelis_mariomarin.MoviesViewModel
import com.example.upelis_mariomarin.viewmodel.PlaylistsViewModel

sealed class BottomNavItem(val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("Home", Icons.Default.Home)
    object Top : BottomNavItem("Top", Icons.Default.Star)
    object Search : BottomNavItem("Buscar", Icons.Default.Search)
    object Favs : BottomNavItem("Favs", Icons.Default.Favorite)
}

@Composable
fun MainScreen(
    navController: NavHostController,                 // <-- Añadido navController
    moviesViewModel: MoviesViewModel,
    authViewModel: AuthViewModel,
    playlistsViewModel: PlaylistsViewModel,           // <-- Añadido playlistsViewModel
    onLogout: () -> Unit,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedItem by remember { mutableStateOf<BottomNavItem>(BottomNavItem.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(
                    BottomNavItem.Home,
                    BottomNavItem.Top,
                    BottomNavItem.Search,
                    BottomNavItem.Favs
                ).forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selectedItem == item,
                        onClick = { selectedItem = item }
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        when (selectedItem) {
            is BottomNavItem.Home -> {
                MoviesScreen(
                    moviesViewModel = moviesViewModel,
                    authViewModel = authViewModel,
                    onLogout = onLogout,
                    onMovieClick = onMovieClick,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is BottomNavItem.Top -> {
                TopScreen(
                    moviesViewModel = moviesViewModel,
                    onMovieClick = onMovieClick,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is BottomNavItem.Search -> {
                SearchScreen(
                    moviesViewModel = moviesViewModel,
                    onMovieClick = onMovieClick,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is BottomNavItem.Favs -> {
                FavsScreen(
                    navController = navController,                // <-- PASAR navController
                    playlistsViewModel = playlistsViewModel,      // <-- PASAR playlistsViewModel
                    moviesViewModel = moviesViewModel,            // <-- PASAR moviesViewModel
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
