package com.misterio.curakid.core.common

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsManagerTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = CoroutineScope(testDispatcher + Job())
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var settingsManager: SettingsManager
    private val testFile = File("test_settings_prefs.preferences_pb")

    @Before
    fun setup() {
        if (testFile.exists()) testFile.delete()
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        settingsManager = SettingsManager(dataStore)
    }

    @After
    fun cleanup() {
        testScope.cancel()
        if (testFile.exists()) testFile.delete()
    }

    @Test
    fun `saveAppEnabled should update value`() = runTest(testDispatcher) {
        settingsManager.saveAppEnabled(false)
        assertFalse(settingsManager.isAppEnabled.first())

        settingsManager.saveAppEnabled(true)
        assertTrue(settingsManager.isAppEnabled.first())
    }

    @Test
    fun `saveNotificationsEnabled should update value`() = runTest(testDispatcher) {
        settingsManager.saveNotificationsEnabled(false)
        assertFalse(settingsManager.isNotificationsEnabled.first())

        settingsManager.saveNotificationsEnabled(true)
        assertTrue(settingsManager.isNotificationsEnabled.first())
    }

    @Test
    fun `saveLastSyncTime and getLastSyncTime should work`() = runTest(testDispatcher) {
        val childId = "c1"
        val time = 123456789L
        
        settingsManager.saveLastSyncTime(childId, time)
        assertEquals(time, settingsManager.getLastSyncTime(childId))
    }
}
