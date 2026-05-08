package com.example.reclaim.domain.blocking

interface ForegroundAppMonitor {
    fun currentForegroundPackage(): String?
}
