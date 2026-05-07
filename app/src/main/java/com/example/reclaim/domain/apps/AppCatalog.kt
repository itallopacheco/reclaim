package com.example.reclaim.domain.apps

interface AppCatalog {
    fun installedApps(): List<App>
}
