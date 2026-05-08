package com.example.reclaim.domain.blocking.fakes

import com.example.reclaim.domain.blocking.BlockingDecision

class FakeBlockingDecision(private val blocked: Set<String> = emptySet()) : BlockingDecision {
    override fun isBlocked(packageName: String): Boolean = packageName in blocked
}
