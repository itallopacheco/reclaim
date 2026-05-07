package com.example.reclaim.domain.apps

data class App(
    val packageName: String,
    val displayName: String,
    val isLauncherApp: Boolean,
)
