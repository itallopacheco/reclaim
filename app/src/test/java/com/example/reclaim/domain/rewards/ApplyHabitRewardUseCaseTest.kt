package com.example.reclaim.domain.rewards

import com.example.reclaim.domain.rewards.fakes.FakeRewardsRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class ApplyHabitRewardUseCaseTest {

    @Test
    fun `invoke adds reward to current balance`() {
        val repo = FakeRewardsRepository(initial = 10.minutes)

        ApplyHabitRewardUseCase(repo).invoke(reward = 30.minutes)

        assertEquals(40.minutes, repo.currentBalance())
    }
}
