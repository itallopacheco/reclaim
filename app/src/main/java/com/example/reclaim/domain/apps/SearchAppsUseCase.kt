package com.example.reclaim.domain.apps

class SearchAppsUseCase(
    private val catalog: AppCatalog,
    private val addedApps: AddedAppsRepository,
) {
    fun invoke(query: String): List<App> {
        val added = addedApps.addedPackageNames()
        return catalog.installedApps()
            .filter { it.packageName !in added }
            .filter { it.displayName.contains(query, ignoreCase = true) }
    }
}
