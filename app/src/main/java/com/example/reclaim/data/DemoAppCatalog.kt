package com.example.reclaim.data

import com.example.reclaim.domain.apps.App
import com.example.reclaim.domain.apps.AppCatalog

/**
 * Static demo catalog so the modal renders content before fatia C wires
 * up the real PackageManager-backed implementation.
 */
object DemoAppCatalog : AppCatalog {
    override fun installedApps(): List<App> = listOf(
        App("com.instagram.android", "Instagram", isLauncherApp = true),
        App("com.whatsapp", "WhatsApp", isLauncherApp = true),
        App("com.zhiliaoapp.musically", "TikTok", isLauncherApp = true),
        App("com.google.android.youtube", "YouTube", isLauncherApp = true),
        App("com.twitter.android", "Twitter / X", isLauncherApp = true),
        App("com.snapchat.android", "Snapchat", isLauncherApp = true),
        App("com.linkedin.android", "LinkedIn", isLauncherApp = true),
        App("com.reddit.frontpage", "Reddit", isLauncherApp = true),
        App("com.spotify.music", "Spotify", isLauncherApp = true),
        App("com.netflix.mediaclient", "Netflix", isLauncherApp = true),
    )
}
