package com.example.reclaim.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreAddedAppsRepositoryTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    @Test
    fun `addedApps is empty on a fresh store`() {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = TestScope(),
            produceFile = { tmpFolder.newFile("test.preferences_pb") },
        )
        val repository = DataStoreAddedAppsRepository(dataStore)

        assertTrue(repository.addedApps().isEmpty())
    }
}
