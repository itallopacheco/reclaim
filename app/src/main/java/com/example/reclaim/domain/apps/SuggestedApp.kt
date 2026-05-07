package com.example.reclaim.domain.apps

import kotlin.time.Duration

data class SuggestedApp(
    val app: App,
    val avgDaily: Duration,
)
