package com.example.reclaim.domain.blocking

interface BlockingDecision {
    fun isBlocked(packageName: String): Boolean
}
