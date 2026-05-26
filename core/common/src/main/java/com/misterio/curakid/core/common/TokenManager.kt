package com.misterio.curakid.core.common

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @param:Named("TokenDataStore") private val dataStore: DataStore<Preferences>,
    private val securityManager: SecurityManager,
    scope: CoroutineScope,
) {

    private val refreshTokenKey = stringPreferencesKey("refresh_token")

    private val _accessToken = MutableStateFlow<String?>(null)

    private val _refreshToken = MutableStateFlow<String?>(null)
    val refreshToken: StateFlow<String?> = _refreshToken.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private var refreshAction: (suspend () -> Unit)? = null

    fun setRefreshAction(action: suspend () -> Unit) {
        refreshAction = action
    }

    val currentToken: String?
        get() = _accessToken.value

    val currentRefreshToken: String?
        get() = _refreshToken.value


    val tokenState: StateFlow<TokenState> = combine(
        _accessToken,
        _refreshToken,
        _isInitialized
    ) { accessToken, refreshToken, initialized ->
        when {
            !initialized -> TokenState.Loading
            !accessToken.isNullOrEmpty() -> TokenState.Available(accessToken)
            !refreshToken.isNullOrEmpty() -> TokenState.Available("") // Temporary state while refreshing
            else -> TokenState.Empty
        }
    }.stateIn(scope, SharingStarted.Eagerly, TokenState.Loading)

    init {
        Log.d(TAG, "init: Starting DataStore collection")
        scope.launch {
            dataStore.data
                .catch {
                    Log.e(TAG, "init: Error collecting DataStore", it)
                    emit(emptyPreferences())
                }
                .collect { prefs ->
                    val rt = prefs[refreshTokenKey]
                    val decryptedRt = try {
                        rt?.let { securityManager.decrypt(it) }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to decrypt refresh token. Clearing tokens.", e)
                        // If decryption fails (e.g. migration from plain text), clear tokens
                        scope.launch { clearTokens() }
                        null
                    }
                    Log.d(
                        TAG,
                        "init: Loaded tokens. Access: ${currentToken?.take(10)}..., Refresh: ${
                            decryptedRt?.take(10)
                        }..."
                    )
                    _refreshToken.value = decryptedRt
                    _isInitialized.value = true

                    if (decryptedRt != null && _accessToken.value == null) {
                        scope.launch {
                            refreshAction?.invoke()
                        }
                    }
                }
        }
    }

    suspend fun waitForInitialization() {
        if (_isInitialized.value) {
            Log.d(TAG, "waitForInitialization: Already initialized")
            return
        }
        Log.d(TAG, "waitForInitialization: Waiting...")
        isInitialized.filter { it }.first()
        Log.d(TAG, "waitForInitialization: Done")
    }

    suspend fun save(accessToken: String, refreshToken: String) {
        Log.i(TAG, "saving tokens: $accessToken\n$refreshToken")
        _accessToken.value = accessToken

        val encryptedRt = securityManager.encrypt(refreshToken)

        dataStore.edit { prefs ->
            prefs[refreshTokenKey] = encryptedRt
        }
    }

    suspend fun clearTokens() {
        Log.i(TAG, "clearing tokens")
        _accessToken.value = null
        dataStore.edit { prefs ->
            prefs.remove(refreshTokenKey)
        }
    }

    private companion object {
        private const val TAG = "TokenManager"
    }
}

sealed class TokenState {
    data object Loading : TokenState()
    data object Empty : TokenState()
    data class Available(val token: String) : TokenState()
}