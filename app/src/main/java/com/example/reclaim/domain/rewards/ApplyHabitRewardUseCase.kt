package com.example.reclaim.domain.rewards

import kotlin.time.Duration

class ApplyHabitRewardUseCase(private val rewards: RewardsRepository) {
    fun invoke(reward: Duration) {
        rewards.addReward(reward)
    }
}
