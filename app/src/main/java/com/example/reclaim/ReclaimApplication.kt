package com.example.reclaim

import android.app.Application
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.reclaim.data.DataStoreAddedAppsRepository
import com.example.reclaim.data.DataStoreHabitsRepository
import com.example.reclaim.data.DataStoreRewardsRepository
import com.example.reclaim.data.PackageManagerAppCatalog
import com.example.reclaim.data.UsageEventsForegroundAppMonitor
import com.example.reclaim.data.UsageStatsManagerStats
import com.example.reclaim.domain.apps.AddedAppsRepository
import com.example.reclaim.domain.apps.RankAppsForHomeUseCase
import com.example.reclaim.domain.apps.SearchAppsUseCase
import com.example.reclaim.domain.apps.SuggestAppsUseCase
import com.example.reclaim.domain.apps.TodayScreenTimeUseCase
import com.example.reclaim.domain.blocking.ForegroundAppMonitor
import com.example.reclaim.domain.blocking.ShouldBlockAppUseCase
import com.example.reclaim.domain.habits.HabitsRepository
import com.example.reclaim.domain.habits.HabitsTodaySummaryUseCase
import com.example.reclaim.domain.rewards.ApplyHabitRewardUseCase
import com.example.reclaim.domain.rewards.ApplyHabitUnrewardUseCase
import com.example.reclaim.domain.rewards.ApplyRewardSpendUseCase
import com.example.reclaim.domain.rewards.CurrentRewardBalanceUseCase
import com.example.reclaim.domain.rewards.RewardsRepository

private val Context.addedAppsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "added_apps",
)

private val Context.habitsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "habits",
)

private val Context.rewardsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "rewards",
)

class ReclaimApplication : Application() {

    val appCatalog: PackageManagerAppCatalog by lazy {
        PackageManagerAppCatalog(packageManager, ownPackageName = packageName)
    }

    val usageStats: UsageStatsManagerStats by lazy {
        UsageStatsManagerStats(
            usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager,
            appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager,
            packageName = packageName,
        )
    }

    val addedApps: AddedAppsRepository by lazy {
        DataStoreAddedAppsRepository(addedAppsDataStore)
    }

    val suggestApps: SuggestAppsUseCase
        get() = SuggestAppsUseCase(catalog = appCatalog, usageStats = usageStats, addedApps = addedApps)

    val searchApps: SearchAppsUseCase
        get() = SearchAppsUseCase(catalog = appCatalog, addedApps = addedApps)

    val todayScreenTime: TodayScreenTimeUseCase
        get() = TodayScreenTimeUseCase(addedApps = addedApps, usageStats = usageStats)

    val rankAppsForHome: RankAppsForHomeUseCase
        get() = RankAppsForHomeUseCase(
            addedApps = addedApps,
            usageStats = usageStats,
            catalog = appCatalog,
            blockingDecision = shouldBlockApp,
        )

    val shouldBlockApp: ShouldBlockAppUseCase
        get() = ShouldBlockAppUseCase(addedApps = addedApps, usageStats = usageStats)

    val foregroundAppMonitor: ForegroundAppMonitor by lazy {
        UsageEventsForegroundAppMonitor(
            usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager,
        )
    }

    val habits: HabitsRepository by lazy {
        DataStoreHabitsRepository(habitsDataStore)
    }

    val habitsTodaySummary: HabitsTodaySummaryUseCase
        get() = HabitsTodaySummaryUseCase(habits = habits)

    val rewards: RewardsRepository by lazy {
        DataStoreRewardsRepository(rewardsDataStore)
    }

    val currentRewardBalance: CurrentRewardBalanceUseCase
        get() = CurrentRewardBalanceUseCase(rewards = rewards)

    val applyRewardSpend: ApplyRewardSpendUseCase
        get() = ApplyRewardSpendUseCase(rewards = rewards)

    val applyHabitReward: ApplyHabitRewardUseCase
        get() = ApplyHabitRewardUseCase(rewards = rewards)

    val applyHabitUnreward: ApplyHabitUnrewardUseCase
        get() = ApplyHabitUnrewardUseCase(rewards = rewards)
}

fun Context.reclaimApplication(): ReclaimApplication =
    applicationContext as ReclaimApplication
