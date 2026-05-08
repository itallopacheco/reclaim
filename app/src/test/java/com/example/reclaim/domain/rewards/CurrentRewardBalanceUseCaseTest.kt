package com.example.reclaim.domain.rewards

import com.example.reclaim.domain.rewards.fakes.FakeRewardsRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class CurrentRewardBalanceUseCaseTest {

    @Test
    fun `invoke returns the repository balance`() {
        val repo = FakeRewardsRepository(initial = 17.minutes)

        val balance = CurrentRewardBalanceUseCase(repo).invoke()

        assertEquals(17.minutes, balance)
    }
}
