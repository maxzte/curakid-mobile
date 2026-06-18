package com.misterio.curakid.core.common

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    @param:Named("SettingsDataStore") private val dataStore: DataStore<Preferences>
) {
    private val isNotificationsEnabledKey = booleanPreferencesKey("is_notifications_enabled")

    val isNotificationsEnabled: Flow<Boolean> =
        dataStore.data.map { it[isNotificationsEnabledKey] ?: true }

    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[isNotificationsEnabledKey] = enabled }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    suspend fun saveLastSyncTime(childId: String, time: Long) {
        val key = longPreferencesKey("last_sync_time_$childId")
        dataStore.edit { it[key] = time }
    }

    suspend fun getLastSyncTime(childId: String): Long {
        val key = longPreferencesKey("last_sync_time_$childId")
        return dataStore.data.map { it[key] ?: 0L }.first()
    }

}
