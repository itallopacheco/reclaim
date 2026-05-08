package com.example.reclaim.ui.screen

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import com.example.reclaim.ui.theme.ReclaimTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class BlockingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appName = intent.getStringExtra(EXTRA_APP_NAME).orEmpty()
        val usageToday: Duration = intent.getLongExtra(EXTRA_USAGE_TODAY_MS, 0L).milliseconds
        val dailyQuota: Duration = intent.getLongExtra(EXTRA_DAILY_QUOTA_MS, 0L).milliseconds

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    exitToHome()
                }
            },
        )

        setContent {
            ReclaimTheme {
                BlockedAppScreenContent(
                    appName = appName,
                    usageToday = usageToday,
                    dailyQuota = dailyQuota,
                    onExit = ::exitToHome,
                )
            }
        }
    }

    private fun exitToHome() {
        val home = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(home)
        finish()
    }

    companion object {
        const val EXTRA_APP_NAME = "appName"
        const val EXTRA_USAGE_TODAY_MS = "usageTodayMs"
        const val EXTRA_DAILY_QUOTA_MS = "dailyQuotaMs"
    }
}
