package com.example.reclaim.domain.rewards.fakes

import com.example.reclaim.domain.rewards.RewardsRepository
import kotlin.time.Duration

class FakeRewardsRepository(initial: Duration = Duration.ZERO) : RewardsRepository {
    private var balance: Duration = initial

    override fun currentBalance(): Duration = balance

    override fun addReward(amount: Duration) {
        balance += amount
    }

    override fun subtractReward(amount: Duration) {
        balance -= amount
    }

    override fun spend(amount: Duration) {
        balance = (balance - amount).coerceAtLeast(Duration.ZERO)
    }
}
