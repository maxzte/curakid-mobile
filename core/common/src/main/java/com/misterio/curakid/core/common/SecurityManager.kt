package com.misterio.curakid.core.common

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Serializable
enum class PasscodeMode {
    SETUP,
    UNLOCK,
    DISABLE
}

@Singleton
open class SecurityManager @Inject constructor(
    @param:Named("SecurityDataStore") private val dataStore: DataStore<Preferences>
) {
    private val passcodeKey = stringPreferencesKey("encrypted_passcode")
    private val biometricsEnabledKey = androidx.datastore.preferences.core.booleanPreferencesKey("biometrics_enabled")
    private val lockoutUntilKey = androidx.datastore.preferences.core.longPreferencesKey("passcode_lockout_until")
    private val failedAttemptsKey = androidx.datastore.preferences.core.intPreferencesKey("passcode_failed_attempts")
    private val alias = "passcode_key"
    private val transformation = "AES/GCM/NoPadding"
    private val provider = "AndroidKeyStore"

    val isPasscodeEnabled: Flow<Boolean> = dataStore.data.map {
        !it[passcodeKey].isNullOrEmpty()
    }

    val isBiometricsEnabled: Flow<Boolean> = dataStore.data.map {
        it[biometricsEnabledKey] ?: true // enabled by default if hardware supports
    }

    val lockoutUntilMillis: Flow<Long> = dataStore.data.map {
        it[lockoutUntilKey] ?: 0L
    }

    val failedAttempts: Flow<Int> = dataStore.data.map {
        it[failedAttemptsKey] ?: 0
    }

    suspend fun setLockout(untilMillis: Long, attempts: Int) {
        dataStore.edit {
            it[lockoutUntilKey] = untilMillis
            it[failedAttemptsKey] = attempts
        }
    }

    suspend fun resetLockout() {
        dataStore.edit {
            it.remove(lockoutUntilKey)
            it.remove(failedAttemptsKey)
        }
    }

    suspend fun setBiometricsEnabled(enabled: Boolean) {
        dataStore.edit { it[biometricsEnabledKey] = enabled }
    }

    suspend fun savePasscode(passcode: String) {
        val encrypted = encrypt(passcode)
        dataStore.edit { it[passcodeKey] = encrypted }
    }

    suspend fun clearPasscode() {
        dataStore.edit {
            it.remove(passcodeKey)
            it.remove(lockoutUntilKey)
            it.remove(failedAttemptsKey)
        }
    }

    suspend fun verifyPasscode(enteredPasscode: String): Boolean {
        val storedEncrypted = dataStore.data.map { it[passcodeKey] }.first() ?: return false
        return try {
            val decrypted = decrypt(storedEncrypted)
            java.security.MessageDigest.isEqual(
                decrypted.toByteArray(Charsets.UTF_8),
                enteredPasscode.toByteArray(Charsets.UTF_8)
            )
        } catch (e: Exception) {
            false
        }
    }

    open fun encrypt(data: String): String {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        val iv = cipher.iv

        // Combine IV and encrypted data for storage
        val combined = iv + encryptedBytes
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    open fun decrypt(encryptedData: String): String {
        val combined = try {
            Base64.decode(encryptedData, Base64.NO_WRAP)
        } catch (e: Exception) {
            // Fallback for cases where it was encoded with DEFAULT or is not base64
            Base64.decode(encryptedData, Base64.DEFAULT)
        }

        if (combined.size < 12) throw IllegalArgumentException("Invalid encrypted data")

        val iv = combined.sliceArray(0 until 12) // GCM IV is 12 bytes
        val encryptedBytes = combined.sliceArray(12 until combined.size)

        val cipher = Cipher.getInstance(transformation)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)

        return String(cipher.doFinal(encryptedBytes))
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(provider)
        keyStore.load(null)

        keyStore.getKey(alias, null)?.let { return it as SecretKey }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, provider)
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
}
