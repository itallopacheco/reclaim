package com.example.reclaim.navigation

sealed class Destination(val route: String) {
    data object OnboardingValue : Destination("onboarding/value")
    data object OnboardingPermissions : Destination("onboarding/permissions")

    // Main app (tab bar destinations)
    data object Home : Destination("main/home")
    data object Apps : Destination("main/apps")
    data object Habits : Destination("main/habits")

    // Modals
    data object AddApp : Destination("modal/add-app")
    data object AddHabit : Destination("modal/add-habit")

    // Lock screen, opened from app list
    data object Lock : Destination("lock")
}

enum class TabDestination(val destination: Destination, val label: String) {
    Home(Destination.Home, "Home"),
    Apps(Destination.Apps, "Apps"),
    Habits(Destination.Habits, "Habits")
}
