package com.misterio.curakid.core.common

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")

@Singleton
class SettingsManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val isAppEnabledKey = booleanPreferencesKey("is_app_enabled")
    private val isNotificationsEnabledKey = booleanPreferencesKey("is_notifications_enabled")

    val isAppEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[isAppEnabledKey] ?: true }
    val isNotificationsEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[isNotificationsEnabledKey] ?: true }

    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[isNotificationsEnabledKey] = enabled }
    }

    suspend fun saveAppEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[isAppEnabledKey] = enabled }
    }

    suspend fun clear() {
        context.settingsDataStore.edit { it.clear() }
    }

    suspend fun saveLastSyncTime(childId: String, time: Long) {
        val key = longPreferencesKey("last_sync_time_$childId")
        context.settingsDataStore.edit { it[key] = time }
    }

    suspend fun getLastSyncTime(childId: String): Long {
        val key = longPreferencesKey("last_sync_time_$childId")
        return context.settingsDataStore.data.map { it[key] ?: 0L }.first()
    }

}
