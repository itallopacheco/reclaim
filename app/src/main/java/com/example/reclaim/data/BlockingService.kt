package com.example.reclaim.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import com.example.reclaim.R
import com.example.reclaim.domain.apps.AddedApp
import com.example.reclaim.domain.apps.AddedAppsRepository
import com.example.reclaim.domain.apps.AppCatalog
import com.example.reclaim.domain.apps.UsageStats
import com.example.reclaim.domain.blocking.ForegroundAppMonitor
import com.example.reclaim.domain.blocking.ShouldBlockAppUseCase
import com.example.reclaim.reclaimApplication
import com.example.reclaim.ui.screen.BlockingActivity
import kotlin.time.Duration.Companion.seconds

class BlockingService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var foregroundAppMonitor: ForegroundAppMonitor
    private lateinit var shouldBlockApp: ShouldBlockAppUseCase
    private lateinit var addedApps: AddedAppsRepository
    private lateinit var usageStats: UsageStats
    private lateinit var appCatalog: AppCatalog
    private var lastBlockedDispatched: String? = null

    private val tick = object : Runnable {
        override fun run() {
            try {
                pollOnce()
            } catch (t: Throwable) {
                Log.w(TAG, "tick failed", t)
            } finally {
                handler.postDelayed(this, POLL_INTERVAL.inWholeMilliseconds)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val app = reclaimApplication()
        foregroundAppMonitor = app.foregroundAppMonitor
        shouldBlockApp = app.shouldBlockApp
        addedApps = app.addedApps
        usageStats = app.usageStats
        appCatalog = app.appCatalog
        startForegroundWithNotification()
        handler.post(tick)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        super.onDestroy()
    }

    private fun pollOnce() {
        if (addedApps.addedApps().isEmpty()) {
            stopSelf()
            return
        }
        val current = foregroundAppMonitor.currentForegroundPackage()
        if (current == null) {
            lastBlockedDispatched = null
            return
        }
        if (current != lastBlockedDispatched) {
            lastBlockedDispatched = null
        }
        if (!shouldBlockApp.invoke(current)) {
            lastBlockedDispatched = null
            return
        }
        if (current == lastBlockedDispatched) return
        if (!Settings.canDrawOverlays(this)) {
            Log.i(TAG, "skipping overlay: SYSTEM_ALERT_WINDOW not granted")
            return
        }
        dispatchOverlay(current)
        lastBlockedDispatched = current
    }

    private fun dispatchOverlay(packageName: String) {
        val added = addedApps.addedApps().firstOrNull { it.packageName == packageName } ?: return
        val displayName = appCatalog.installedApps()
            .firstOrNull { it.packageName == packageName }?.displayName
            ?: packageName
        val today = usageStats.usageToday()[packageName] ?: kotlin.time.Duration.ZERO
        val intent = Intent(this, BlockingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(BlockingActivity.EXTRA_APP_NAME, displayName)
            putExtra(BlockingActivity.EXTRA_USAGE_TODAY_MS, today.inWholeMilliseconds)
            putExtra(BlockingActivity.EXTRA_DAILY_QUOTA_MS, added.dailyQuota.inWholeMilliseconds)
        }
        startActivity(intent)
    }

    private fun startForegroundWithNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Reclaim",
                    NotificationManager.IMPORTANCE_LOW,
                ),
            )
        }
        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Reclaim")
            .setContentText("Reclaim está protegendo seus limites")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val TAG = "BlockingService"
        private const val CHANNEL_ID = "reclaim_blocking"
        private const val NOTIFICATION_ID = 1001
        private val POLL_INTERVAL = 1.seconds

        fun start(context: Context) {
            val intent = Intent(context, BlockingService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, BlockingService::class.java))
        }
    }
}
