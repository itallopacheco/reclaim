package com.example.reclaim.domain.rewards

import com.example.reclaim.domain.rewards.fakes.FakeRewardsRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ApplyRewardSpendUseCaseTest {

    @Test
    fun `spend reduces balance by elapsed`() {
        val repo = FakeRewardsRepository(initial = 30.minutes)

        ApplyRewardSpendUseCase(repo).invoke(elapsed = 10.minutes)

        assertEquals(20.minutes, repo.currentBalance())
    }

    @Test
    fun `spend never drives balance below zero`() {
        val repo = FakeRewardsRepository(initial = 30.seconds)

        ApplyRewardSpendUseCase(repo).invoke(elapsed = 90.seconds)

        assertEquals(Duration.ZERO, repo.currentBalance())
    }
}
