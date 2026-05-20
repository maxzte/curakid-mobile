package com.misterio.core.common

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
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
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    scope: CoroutineScope,
) {

    private val refreshTokenKey = stringPreferencesKey("refresh_token")

    private val _accessToken = MutableStateFlow<String?>(null)

    private val _refreshToken = MutableStateFlow<String?>(null)
    val refreshToken: StateFlow<String?> = _refreshToken.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    val currentToken: String?
        get() = _accessToken.value

    val currentRefreshToken: String?
        get() = _refreshToken.value


    val tokenState: StateFlow<TokenState> = _accessToken
        .combine(_isInitialized) { token, initialized ->
            when {
                !initialized -> TokenState.Loading
                token.isNullOrEmpty() -> TokenState.Empty
                else -> TokenState.Available(token)
            }
        }.stateIn(scope, SharingStarted.Eagerly, TokenState.Loading)

    init {
        Log.d(TAG, "init: Starting DataStore collection")
        scope.launch {
            context.dataStore.data
                .catch { 
                    Log.e(TAG, "init: Error collecting DataStore", it)
                    emit(emptyPreferences()) 
                }
                .collect { prefs ->
                val rt = prefs[refreshTokenKey]
                Log.d(TAG, "init: Loaded tokens. Access: ${currentToken?.take(10)}..., Refresh: ${rt?.take(10)}...")
                _refreshToken.value = rt
                _isInitialized.value = true
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

        context.dataStore.edit { prefs ->
            prefs[refreshTokenKey] = refreshToken
        }
    }

    suspend fun clearTokens() {
        Log.i(TAG, "clearing tokens")
        _accessToken.value = null
        context.dataStore.edit { prefs ->
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