package com.example.reclaim.domain.apps

import kotlin.time.Duration

data class AddedApp(
    val packageName: String,
    val dailyQuota: Duration,
)
