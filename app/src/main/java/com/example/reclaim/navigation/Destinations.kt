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
    data object EditApp : Destination("modal/edit-app/{packageName}") {
        const val ARG_PACKAGE_NAME = "packageName"
        fun routeFor(packageName: String) = "modal/edit-app/$packageName"
    }
    data object EditHabit : Destination("modal/edit-habit/{id}") {
        const val ARG_ID = "id"
        fun routeFor(id: Long) = "modal/edit-habit/$id"
    }

    // Lock screen, opened from app list
    data object Lock : Destination("lock")
}

enum class TabDestination(val destination: Destination, val label: String) {
    Home(Destination.Home, "Home"),
    Apps(Destination.Apps, "Apps"),
    Habits(Destination.Habits, "Habits")
}
