package com.example.reclaim.domain.apps

import kotlin.time.Duration

data class HomeAppRow(
    val app: App,
    val today: Duration,
    val quota: Duration,
    val status: HomeAppStatus,
)
