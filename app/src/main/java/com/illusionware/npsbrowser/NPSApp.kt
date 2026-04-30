package com.illusionware.npsbrowser

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.illusionware.npsbrowser.data.OnboardingPreferencesRepository
import com.illusionware.npsbrowser.data.SettingsPreferencesRepository
import com.illusionware.npsbrowser.data.download.DownloadManager
import com.illusionware.npsbrowser.data.notification.Notifications

@OptIn(ExperimentalUnsignedTypes::class)
val HMAC_KEY = ubyteArrayOf(
    0xE5U, 0xE2U, 0x78U, 0xAAU, 0x1EU, 0xE3U, 0x40U, 0x82U, 0xA0U, 0x88U, 0x27U, 0x9CU, 0x83U, 0xF9U, 0xBBU, 0xC8U,
    0x06U, 0x82U, 0x1CU, 0x52U, 0xF2U, 0xABU, 0x5DU, 0x2BU, 0x4AU, 0xBDU, 0x99U, 0x54U, 0x50U, 0x35U, 0x51U, 0x14U
)

private const val PREFERENCES_NAME = "preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCES_NAME
)

class NPSApp: Application() {
    lateinit var settingsPreferencesRepository: SettingsPreferencesRepository
    lateinit var onboardingPreferencesRepository: OnboardingPreferencesRepository
    lateinit var downloadManager: DownloadManager

    override fun onCreate() {
        super.onCreate()
        settingsPreferencesRepository = SettingsPreferencesRepository(dataStore)
        onboardingPreferencesRepository = OnboardingPreferencesRepository(dataStore)
//        downloader = Downloader(this)
        downloadManager = DownloadManager(this)

        Notifications.initialize(this)
    }
}