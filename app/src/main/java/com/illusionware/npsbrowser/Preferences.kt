package com.illusionware.npsbrowser

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

class Preferences(private val context: Context) {
    companion object {
        private val Context.dataStore  by preferencesDataStore(name = "settings")
        private val APP_THEME = intPreferencesKey("app_theme")
        private val DATA_LAYOUT = intPreferencesKey("layout")
        private val SEEN_ONBOARDING = booleanPreferencesKey("seen_onboarding")
    }

    val theme = context.dataStore.data.map { prefs ->
        prefs[APP_THEME] ?: 2
    }

    val layout = context.dataStore.data.map{ prefs ->
        prefs[DATA_LAYOUT] ?: 0
    }
    
    val seenOnboarding = context.dataStore.data.map{ prefs ->
        prefs[SEEN_ONBOARDING] ?: false
    }

    suspend fun changeTheme(theme: Int) {
        context.dataStore.edit { prefs ->
            prefs[APP_THEME] = theme
        }
    }

    suspend fun changeLayout(layout: Int) {
        context.dataStore.edit { prefs ->
            prefs[DATA_LAYOUT] = layout
        }
    }
    
    suspend fun finishOnboarding() {
        context.dataStore.edit { prefs ->
            prefs[SEEN_ONBOARDING] = true
        }
    }
}