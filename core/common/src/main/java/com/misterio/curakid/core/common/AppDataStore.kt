package com.misterio.curakid.core.common

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

@Singleton
class AppDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")
    private val selectedChildIdKey = stringPreferencesKey("selected_child_id")
    private val lastChildrenSyncKey = longPreferencesKey("last_children_sync")

    val lastChildrenSync: Flow<Long> = context.appDataStore.data
        .catch {
            Log.e(TAG, "Error reading AppDataStore", it)
            emit(emptyPreferences())
        }
        .map { it[lastChildrenSyncKey] ?: 0L }

    val selectedChildId: Flow<String?> = context.appDataStore.data.map { it[selectedChildIdKey] }

    val onboardingCompleted: Flow<Boolean> = context.appDataStore.data
        .catch {
            Log.e(TAG, "Error reading AppDataStore", it)
            emit(emptyPreferences())
        }
        .map { it[onboardingCompletedKey] ?: false }
        .distinctUntilChanged()

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.appDataStore.edit { it[onboardingCompletedKey] = completed }
    }

    suspend fun saveSelectedChildId(id: String?) {
        context.appDataStore.edit { prefs ->
            if (id != null) {
                prefs[selectedChildIdKey] = id
            } else {
                prefs.remove(selectedChildIdKey)
            }
        }
    }

    suspend fun setLastChildrenSync(timestamp: Long) {
        context.appDataStore.edit { it[lastChildrenSyncKey] = timestamp }
    }

    suspend fun saveLastNormsSyncTime(type: String, gender: String, time: Long) {
        val key = longPreferencesKey("norms_sync_time_${type}_$gender")
        context.appDataStore.edit { it[key] = time }
    }

    suspend fun getLastNormsSyncTime(type: String, gender: String): Long {
        val key = longPreferencesKey("norms_sync_time_${type}_$gender")
        return context.appDataStore.data.map { it[key] ?: 0L }.first()
    }

    suspend fun clear() {
        context.appDataStore.edit { it.clear() }
    }

    private companion object {
        private const val TAG = "AppDataStore"
    }
}
