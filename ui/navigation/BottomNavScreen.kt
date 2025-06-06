package com.example.upelis_mariomarin.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavScreen(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavScreen("home", "Home", Icons.Filled.Home)
    object Top : BottomNavScreen("top", "Top", Icons.Filled.Star)
    object Search : BottomNavScreen("search", "Buscar", Icons.Filled.Search)
    object Favs : BottomNavScreen("favs", "Favs", Icons.Filled.Favorite)
}
