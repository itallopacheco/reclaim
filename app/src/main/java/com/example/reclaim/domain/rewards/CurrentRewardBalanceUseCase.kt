package com.example.reclaim.domain.rewards

import kotlin.time.Duration

class CurrentRewardBalanceUseCase(private val rewards: RewardsRepository) {
    fun invoke(): Duration = rewards.currentBalance()
}
