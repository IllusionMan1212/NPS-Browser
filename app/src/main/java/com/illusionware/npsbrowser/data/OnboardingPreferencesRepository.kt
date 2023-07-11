package com.illusionware.npsbrowser.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

data class OnboardingPreferences(
    val seenOnboarding: Boolean,
)

class OnboardingPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        private val SEEN_ONBOARDING = booleanPreferencesKey("seen_onboarding")
    }

    fun getPreferences(): Flow<OnboardingPreferences> =
        runBlocking { dataStore.data.map { mapOnboardingSettings(it) } }

    suspend fun setSeenOnboarding(seen: Boolean) {
        dataStore.edit { prefs ->
            prefs[SEEN_ONBOARDING] = seen
        }
    }

    private fun mapOnboardingSettings(prefs: Preferences): OnboardingPreferences {
        return OnboardingPreferences(
            prefs[SEEN_ONBOARDING] ?: false,
        )
    }
}