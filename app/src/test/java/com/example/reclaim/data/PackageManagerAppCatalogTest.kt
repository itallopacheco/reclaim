package com.example.reclaim.data

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.ResolveInfo
import androidx.test.core.app.ApplicationProvider
import com.example.reclaim.domain.apps.App
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PackageManagerAppCatalogTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val packageManager = context.packageManager
    private val shadow = shadowOf(packageManager)

    private fun installLauncherApp(packageName: String, label: String) {
        val packageInfo = PackageInfo().apply {
            this.packageName = packageName
            applicationInfo = ApplicationInfo().apply {
                this.packageName = packageName
                name = label
                nonLocalizedLabel = label
            }
        }
        shadow.installPackage(packageInfo)

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfo = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                this.packageName = packageName
                name = "$packageName.MainActivity"
                applicationInfo = packageInfo.applicationInfo
                nonLocalizedLabel = label
            }
        }
        shadow.addResolveInfoForIntent(intent, resolveInfo)
    }

    @Test
    fun `installedApps returns a launcher app present in the package manager`() {
        installLauncherApp("com.example.foo", "Foo")
        val catalog = PackageManagerAppCatalog(packageManager, ownPackageName = "com.example.reclaim")

        assertEquals(
            listOf(App("com.example.foo", "Foo", isLauncherApp = true)),
            catalog.installedApps(),
        )
    }

    @Test
    fun `installedApps excludes apps installed without a launcher activity`() {
        installLauncherApp("com.example.foo", "Foo")
        // bar is installed but not registered for ACTION_MAIN + CATEGORY_LAUNCHER
        shadow.installPackage(PackageInfo().apply {
            packageName = "com.example.bar"
            applicationInfo = ApplicationInfo().apply { packageName = "com.example.bar" }
        })
        val catalog = PackageManagerAppCatalog(packageManager, ownPackageName = "com.example.reclaim")

        val packages = catalog.installedApps().map { it.packageName }
        assertEquals(listOf("com.example.foo"), packages)
    }

    @Test
    fun `installedApps caches the first read until invalidate is called`() {
        installLauncherApp("com.example.foo", "Foo")
        val catalog = PackageManagerAppCatalog(packageManager, ownPackageName = "com.example.reclaim")
        catalog.installedApps() // prime cache

        installLauncherApp("com.example.bar", "Bar")
        val cached = catalog.installedApps().map { it.packageName }

        catalog.invalidate()
        val refreshed = catalog.installedApps().map { it.packageName }

        assertEquals(
            listOf(listOf("com.example.foo"), listOf("com.example.foo", "com.example.bar").sorted()),
            listOf(cached.sorted(), refreshed.sorted()),
        )
    }
}
