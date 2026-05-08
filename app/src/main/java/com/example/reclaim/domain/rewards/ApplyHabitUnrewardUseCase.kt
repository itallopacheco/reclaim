package com.example.reclaim.domain.rewards

import kotlin.time.Duration

class ApplyHabitUnrewardUseCase(private val rewards: RewardsRepository) {
    fun invoke(reward: Duration) {
        rewards.subtractReward(reward)
    }
}
