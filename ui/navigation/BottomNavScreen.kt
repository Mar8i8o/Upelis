package com.example.upelis_mariomarin.ui.navigation

sealed class BottomNavScreen(val route: String, val label: String) {
    object Home : BottomNavScreen("home", "Home")
    object Top : BottomNavScreen("top", "Top")
    object Search : BottomNavScreen("search", "Buscar")
    object Favs : BottomNavScreen("favs", "Favs")
}
