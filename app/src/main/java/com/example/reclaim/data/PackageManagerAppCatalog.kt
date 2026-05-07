package com.example.reclaim.data

import android.content.Intent
import android.content.pm.PackageManager
import com.example.reclaim.domain.apps.App
import com.example.reclaim.domain.apps.AppCatalog

class PackageManagerAppCatalog(
    private val packageManager: PackageManager,
    private val ownPackageName: String,
) : AppCatalog {

    override fun installedApps(): List<App> {
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        return packageManager.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != ownPackageName }
            .map { resolveInfo ->
                App(
                    packageName = resolveInfo.activityInfo.packageName,
                    displayName = resolveInfo.loadLabel(packageManager).toString(),
                    isLauncherApp = true,
                )
            }
    }
}
