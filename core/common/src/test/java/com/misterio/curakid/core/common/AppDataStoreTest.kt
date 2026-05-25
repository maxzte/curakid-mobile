package com.misterio.curakid.core.common

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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
class AppDataStoreTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = CoroutineScope(testDispatcher + Job())
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var appDataStore: AppDataStore
    private val testFile = File("test_app_prefs.preferences_pb")

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0

        if (testFile.exists()) testFile.delete()
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        appDataStore = AppDataStore(dataStore)
    }

    @After
    fun cleanup() {
        testScope.cancel()
        if (testFile.exists()) testFile.delete()
        unmockkStatic(Log::class)
    }

    @Test
    fun `setOnboardingCompleted should update value`() = runTest(testDispatcher) {
        appDataStore.setOnboardingCompleted(true)
        assertTrue(appDataStore.onboardingCompleted.first())

        appDataStore.setOnboardingCompleted(false)
        assertFalse(appDataStore.onboardingCompleted.first())
    }

    @Test
    fun `saveSelectedChildId should update value`() = runTest(testDispatcher) {
        val childId = "child-123"
        appDataStore.saveSelectedChildId(childId)
        assertEquals(childId, appDataStore.selectedChildId.first())

        appDataStore.saveSelectedChildId(null)
        assertEquals(null, appDataStore.selectedChildId.first())
    }

    @Test
    fun `saveLastNormsSyncTime and getLastNormsSyncTime should work`() = runTest(testDispatcher) {
        val type = "weight"
        val gender = "male"
        val time = 987654321L
        
        appDataStore.saveLastNormsSyncTime(type, gender, time)
        assertEquals(time, appDataStore.getLastNormsSyncTime(type, gender))
    }

    @Test
    fun `clear should reset dataStore`() = runTest(testDispatcher) {
        appDataStore.setOnboardingCompleted(true)
        appDataStore.clear()
        
        assertFalse(appDataStore.onboardingCompleted.first())
    }
}
