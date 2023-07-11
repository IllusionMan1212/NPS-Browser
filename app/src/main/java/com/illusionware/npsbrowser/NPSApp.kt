package com.illusionware.npsbrowser

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.illusionware.npsbrowser.data.OnboardingPreferencesRepository
import com.illusionware.npsbrowser.data.SettingsPreferencesRepository

private const val PREFERENCES_NAME = "preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCES_NAME
)

class NPSApp: Application() {
    lateinit var settingsPreferencesRepository: SettingsPreferencesRepository
    lateinit var onboardingPreferencesRepository: OnboardingPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        settingsPreferencesRepository = SettingsPreferencesRepository(dataStore)
        onboardingPreferencesRepository = OnboardingPreferencesRepository(dataStore)
    }
}