package com.misterio.curakid.core.common

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AppDataStore @Inject constructor(
    @param:Named("AppDataStore") private val dataStore: DataStore<Preferences>
) {
    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")
    private val notificationPermissionRequestedKey = booleanPreferencesKey("notification_permission_requested")
    private val selectedChildIdKey = stringPreferencesKey("selected_child_id")
    private val lastChildrenSyncKey = longPreferencesKey("last_children_sync")
    private val lastLifePeriodsSyncKey = longPreferencesKey("last_life_periods_sync")
    private val fcmTokenKey = stringPreferencesKey("fcm_token")

    val lastLifePeriodsSync: Flow<Long> = dataStore.data
        .catch {
            Log.e(TAG, "Error reading AppDataStore", it)
            emit(emptyPreferences())
        }
        .map { it[lastLifePeriodsSyncKey] ?: 0L }

    val lastChildrenSync: Flow<Long> = dataStore.data
        .catch {
            Log.e(TAG, "Error reading AppDataStore", it)
            emit(emptyPreferences())
        }
        .map { it[lastChildrenSyncKey] ?: 0L }

    val selectedChildId: Flow<String?> = dataStore.data.map { it[selectedChildIdKey] }

    val fcmToken: Flow<String?> = dataStore.data.map { it[fcmTokenKey] }

    val onboardingCompleted: Flow<Boolean> = dataStore.data
        .catch {
            Log.e(TAG, "Error reading AppDataStore", it)
            emit(emptyPreferences())
        }
        .map { it[onboardingCompletedKey] ?: false }
        .distinctUntilChanged()

    val notificationPermissionRequested: Flow<Boolean> = dataStore.data
        .catch {
            Log.e(TAG, "Error reading AppDataStore", it)
            emit(emptyPreferences())
        }
        .map { it[notificationPermissionRequestedKey] ?: false }
        .distinctUntilChanged()

    suspend fun setNotificationPermissionRequested(requested: Boolean = true) {
        dataStore.edit { it[notificationPermissionRequestedKey] = requested }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[onboardingCompletedKey] = completed }
    }

    suspend fun saveSelectedChildId(id: String?) {
        dataStore.edit { prefs ->
            if (id != null) {
                prefs[selectedChildIdKey] = id
            } else {
                prefs.remove(selectedChildIdKey)
            }
        }
    }

    suspend fun saveFcmToken(token: String?) {
        dataStore.edit { prefs ->
            if (token != null) {
                prefs[fcmTokenKey] = token
            } else {
                prefs.remove(fcmTokenKey)
            }
        }
    }

    suspend fun setLastChildrenSync(timestamp: Long) {
        dataStore.edit { it[lastChildrenSyncKey] = timestamp }
    }

    suspend fun setLastLifePeriodsSync(timestamp: Long) {
        dataStore.edit { it[lastLifePeriodsSyncKey] = timestamp }
    }

    suspend fun saveLastNormsSyncTime(type: String, gender: String, time: Long) {
        val key = longPreferencesKey("norms_sync_time_${type}_$gender")
        dataStore.edit { it[key] = time }
    }

    suspend fun getLastNormsSyncTime(type: String, gender: String): Long {
        val key = longPreferencesKey("norms_sync_time_${type}_$gender")
        return dataStore.data.map { it[key] ?: 0L }.first()
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private companion object {
        private const val TAG = "AppDataStore"
    }
}
