package com.misterio.curakid.core.common

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")
private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")
private val Context.securityDataStore: DataStore<Preferences> by preferencesDataStore(name = "security_prefs")

@Module
@InstallIn(SingletonComponent::class)
object CommonModule {

    @Provides
    @Singleton
    @Named("TokenDataStore")
    fun provideTokenDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.tokenDataStore

    @Provides
    @Singleton
    @Named("SettingsDataStore")
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.settingsDataStore

    @Provides
    @Singleton
    @Named("AppDataStore")
    fun provideAppDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.appDataStore

    @Provides
    @Singleton
    @Named("SecurityDataStore")
    fun provideSecurityDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.securityDataStore

}
