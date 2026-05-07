package com.example.reclaim.domain.apps

class SearchAppsUseCase(
    private val catalog: AppCatalog,
    private val addedApps: AddedAppsRepository,
) {
    fun invoke(query: String): List<App> =
        catalog.installedApps()
            .filter { it.displayName.contains(query, ignoreCase = true) }
}
