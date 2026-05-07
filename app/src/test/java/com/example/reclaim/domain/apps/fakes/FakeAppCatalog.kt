package com.example.reclaim.domain.apps.fakes

import com.example.reclaim.domain.apps.App
import com.example.reclaim.domain.apps.AppCatalog

class FakeAppCatalog(private val apps: List<App>) : AppCatalog {
    override fun installedApps() = apps
}
