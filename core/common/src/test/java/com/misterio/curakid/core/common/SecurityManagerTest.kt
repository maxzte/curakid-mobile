package com.misterio.curakid.core.common

import android.util.Base64
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SecurityManagerTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = CoroutineScope(testDispatcher + Job())
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var securityManager: SecurityManager
    private val testFile = File("test_security_prefs.preferences_pb")

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0

        mockkStatic(Base64::class)
        every { Base64.encodeToString(any(), any()) } answers {
            java.util.Base64.getEncoder().encodeToString(it.invocation.args[0] as ByteArray)
        }
        every { Base64.decode(any<String>(), any()) } answers {
            java.util.Base64.getDecoder().decode(it.invocation.args[0] as String)
        }

        if (testFile.exists()) testFile.delete()
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )

        securityManager = object : SecurityManager(dataStore) {
            // Override encryption to avoid AndroidKeyStore dependency
            override fun encrypt(data: String): String = data
            override fun decrypt(encryptedData: String): String = encryptedData
        }
    }

    @After
    fun cleanup() {
        testScope.cancel()
        if (testFile.exists()) testFile.delete()
        unmockkStatic(Log::class)
        unmockkStatic(Base64::class)
    }

    @Test
    fun `savePasscode should update isPasscodeEnabled`() = runTest(testDispatcher) {
        assertFalse(securityManager.isPasscodeEnabled.first())

        securityManager.savePasscode("1234")
        assertTrue(securityManager.isPasscodeEnabled.first())
    }

    @Test
    fun `clearPasscode should update isPasscodeEnabled`() = runTest(testDispatcher) {
        securityManager.savePasscode("1234")
        assertTrue(securityManager.isPasscodeEnabled.first())

        securityManager.clearPasscode()
        assertFalse(securityManager.isPasscodeEnabled.first())
    }

    @Test
    fun `verifyPasscode should return true for correct passcode`() = runTest(testDispatcher) {
        val passcode = "1234"
        securityManager.savePasscode(passcode)

        assertTrue(securityManager.verifyPasscode(passcode))
        assertFalse(securityManager.verifyPasscode("wrong"))
    }
}
