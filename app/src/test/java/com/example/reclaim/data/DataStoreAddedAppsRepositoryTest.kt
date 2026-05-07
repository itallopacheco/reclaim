package com.example.reclaim.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.example.reclaim.domain.apps.AddedApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.time.Duration.Companion.minutes

class DataStoreAddedAppsRepositoryTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun newDataStore(name: String = "test.preferences_pb"): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { tmpFolder.newFile(name) },
        )

    @Test
    fun `addedApps is empty on a fresh store`() {
        val repository = DataStoreAddedAppsRepository(newDataStore())

        assertTrue(repository.addedApps().isEmpty())
    }

    @Test
    fun `add then addedApps returns the saved app with its quota`() {
        val repository = DataStoreAddedAppsRepository(newDataStore())

        repository.add(AddedApp("com.example.foo", 30.minutes))

        assertEquals(listOf(AddedApp("com.example.foo", 30.minutes)), repository.addedApps())
    }

    @Test
    fun `adding the same packageName twice keeps a single entry with the latest quota`() {
        val repository = DataStoreAddedAppsRepository(newDataStore())

        repository.add(AddedApp("com.example.foo", 30.minutes))
        repository.add(AddedApp("com.example.foo", 45.minutes))

        assertEquals(listOf(AddedApp("com.example.foo", 45.minutes)), repository.addedApps())
    }
}
