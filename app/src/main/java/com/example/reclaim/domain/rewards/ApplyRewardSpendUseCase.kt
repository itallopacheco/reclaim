package com.example.reclaim.domain.rewards

import kotlin.time.Duration

class ApplyRewardSpendUseCase(private val rewards: RewardsRepository) {
    fun invoke(elapsed: Duration) {
        rewards.spend(elapsed)
    }
}
