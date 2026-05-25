package com.misterio.curakid.core.common

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class TokenManagerTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = CoroutineScope(testDispatcher + Job())
    private val securityManager: SecurityManager = mockk()
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var tokenManager: TokenManager
    private val testFile = File("test_auth_prefs.preferences_pb")

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.i(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0

        if (testFile.exists()) testFile.delete()
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        
        coEvery { securityManager.decrypt(any()) } answers { it.invocation.args[0] as String }
        coEvery { securityManager.encrypt(any()) } answers { it.invocation.args[0] as String }

        tokenManager = TokenManager(dataStore, securityManager, testScope)
    }

    @After
    fun cleanup() {
        testScope.cancel()
        if (testFile.exists()) testFile.delete()
        unmockkStatic(Log::class)
    }

    @Test
    fun `save should update currentToken and dataStore`() = runTest(testDispatcher) {
        val accessToken = "access"
        val refreshToken = "refresh"
        
        tokenManager.save(accessToken, refreshToken)

        assertEquals(accessToken, tokenManager.currentToken)
        assertEquals(refreshToken, tokenManager.currentRefreshToken)
    }

    @Test
    fun `clearTokens should reset values`() = runTest(testDispatcher) {
        tokenManager.save("a", "r")
        
        tokenManager.clearTokens()

        assertEquals(null, tokenManager.currentToken)
        assertEquals(null, tokenManager.currentRefreshToken)
    }
}
