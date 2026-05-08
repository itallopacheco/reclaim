package com.example.reclaim.domain.rewards

import com.example.reclaim.domain.rewards.fakes.FakeRewardsRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class ApplyHabitUnrewardUseCaseTest {

    @Test
    fun `invoke subtracts reward and may go negative`() {
        val repo = FakeRewardsRepository(initial = 5.minutes)

        ApplyHabitUnrewardUseCase(repo).invoke(reward = 10.minutes)

        assertEquals((-5).minutes, repo.currentBalance())
    }
}
