package com.example.reclaim.navigation

sealed class Destination(val route: String) {
    data object Lock : Destination("lock")
    data object Home : Destination("home")
    data object Detail : Destination("detail")
    data object Settings : Destination("settings")
}
