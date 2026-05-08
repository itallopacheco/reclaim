package com.example.reclaim.domain.rewards

import kotlin.time.Duration

interface RewardsRepository {
    fun currentBalance(): Duration
    fun addReward(amount: Duration)
    fun subtractReward(amount: Duration)
    fun spend(amount: Duration)
}
