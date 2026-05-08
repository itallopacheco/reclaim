package com.example.reclaim

import android.app.Application
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.reclaim.data.DataStoreAddedAppsRepository
import com.example.reclaim.data.PackageManagerAppCatalog
import com.example.reclaim.data.UsageStatsManagerStats
import com.example.reclaim.domain.apps.AddedAppsRepository
import com.example.reclaim.domain.apps.SearchAppsUseCase
import com.example.reclaim.domain.apps.SuggestAppsUseCase
import com.example.reclaim.domain.apps.TodayScreenTimeUseCase

private val Context.addedAppsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "added_apps",
)

class ReclaimApplication : Application() {

    val appCatalog: PackageManagerAppCatalog by lazy {
        PackageManagerAppCatalog(packageManager, ownPackageName = packageName)
    }

    val usageStats: UsageStatsManagerStats by lazy {
        UsageStatsManagerStats(
            usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager,
            appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager,
            packageName = packageName,
        )
    }

    val addedApps: AddedAppsRepository by lazy {
        DataStoreAddedAppsRepository(addedAppsDataStore)
    }

    val suggestApps: SuggestAppsUseCase
        get() = SuggestAppsUseCase(catalog = appCatalog, usageStats = usageStats, addedApps = addedApps)

    val searchApps: SearchAppsUseCase
        get() = SearchAppsUseCase(catalog = appCatalog, addedApps = addedApps)

    val todayScreenTime: TodayScreenTimeUseCase
        get() = TodayScreenTimeUseCase(addedApps = addedApps, usageStats = usageStats)
}

fun Context.reclaimApplication(): ReclaimApplication =
    applicationContext as ReclaimApplication
