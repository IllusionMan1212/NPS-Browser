package com.illusionware.npsbrowser

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

enum class ConsoleType {
    PSVITA,
    PS3,
    PSP,
    PSX,
    PSM,
}

enum class DataType {
    GAMES,
    DLC,
    THEMES,
}

enum class Layout {
    LIST,
    GRID,
}

class Preferences(private val context: Context) {
    companion object {
        private val Context.dataStore  by preferencesDataStore("settings")
        private val APP_THEME = intPreferencesKey("app_theme")
        private val DATA_LAYOUT = intPreferencesKey("layout")
        private val SEEN_ONBOARDING = booleanPreferencesKey("seen_onboarding")
        private val PSV_GAMES = stringPreferencesKey("psv_games")
        private val PS3_GAMES = stringPreferencesKey("ps3_games")
        private val PSP_GAMES = stringPreferencesKey("psp_games")
        private val PSX_GAMES = stringPreferencesKey("psx_games")
        private val PSM_GAMES = stringPreferencesKey("psm_games")
        private val PSV_DLC = stringPreferencesKey("psv_dlc")
        private val PS3_DLC = stringPreferencesKey("ps3_dlc")
        private val PSP_DLC = stringPreferencesKey("psp_dlc")
        private val PSV_THEMES = stringPreferencesKey("psv_themes")
        private val HMAC_KEY = stringPreferencesKey("hmac_key")
        private val DOWNLOAD_DIR = stringPreferencesKey("download_dir")
        private val UNPACK_DIR = stringPreferencesKey("unpack_dir")
        private val UNPACK_IN_DOWNLOAD = booleanPreferencesKey("unpack_in_download")
        private val DELETE_AFTER_UNPACK = booleanPreferencesKey("delete_after_unpack")
        private val PKG2ZIP_PARAMS = stringPreferencesKey("pkg2zip_params")
        private val PKG2ZIP_AUTO_DECRYPT = booleanPreferencesKey("pkg2zip_auto_decrypt")
    }

    val theme = context.dataStore.data.map { prefs ->
        prefs[APP_THEME] ?: 2
    }

    val layout = context.dataStore.data.map{ prefs ->
        prefs[DATA_LAYOUT] ?: Layout.LIST.ordinal
    }
    
    val seenOnboarding = context.dataStore.data.map{ prefs ->
        prefs[SEEN_ONBOARDING] ?: false
    }

    val psvGames = context.dataStore.data.map{ prefs ->
        prefs[PSV_GAMES] ?: ""
    }

    val ps3Games = context.dataStore.data.map{ prefs ->
        prefs[PS3_GAMES] ?: ""
    }

    val pspGames = context.dataStore.data.map{ prefs ->
        prefs[PSP_GAMES] ?: ""
    }

    val psxGames = context.dataStore.data.map{ prefs ->
        prefs[PSX_GAMES] ?: ""
    }

    val psmGames = context.dataStore.data.map{ prefs ->
        prefs[PSM_GAMES] ?: ""
    }

    val psvDLC = context.dataStore.data.map{ prefs ->
        prefs[PSV_DLC] ?: ""
    }

    val ps3DLC = context.dataStore.data.map{ prefs ->
        prefs[PS3_DLC] ?: ""
    }

    val pspDLC = context.dataStore.data.map{ prefs ->
        prefs[PSP_DLC] ?: ""
    }

    val psvThemes = context.dataStore.data.map{ prefs ->
        prefs[PSV_THEMES] ?: ""
    }

    val hmacKey = context.dataStore.data.map{ prefs ->
        prefs[HMAC_KEY] ?: ""
    }

    val downloadDir = context.dataStore.data.map{ prefs ->
        prefs[DOWNLOAD_DIR] ?: Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.path ?: ""
    }

    val unpackDir = context.dataStore.data.map{ prefs ->
        prefs[UNPACK_DIR] ?: Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.path ?: ""
    }

    val unpackInDownload = context.dataStore.data.map{ prefs ->
        prefs[UNPACK_IN_DOWNLOAD] ?: false
    }

    val deleteAfterUnpack = context.dataStore.data.map{ prefs ->
        prefs[DELETE_AFTER_UNPACK] ?: false
    }

    val pkg2zipParams = context.dataStore.data.map{ prefs ->
        prefs[PKG2ZIP_PARAMS] ?: "-x {pkgFile} \"{zRifKey}\""
    }

    val pkg2zipAutoDecrypt = context.dataStore.data.map{ prefs ->
        prefs[PKG2ZIP_AUTO_DECRYPT] ?: true
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

    suspend fun setTSVFile(console: ConsoleType, contentType: DataType, file: Uri) {
        context.dataStore.edit { prefs ->
            when (console) {
                ConsoleType.PSVITA -> {
                    when (contentType) {
                        DataType.GAMES -> prefs[PSV_GAMES] = file.toString()
                        DataType.DLC -> prefs[PSV_DLC] = file.toString()
                        DataType.THEMES -> prefs[PSV_THEMES] = file.toString()
                    }
                }
                ConsoleType.PS3 -> {
                    when (contentType) {
                        DataType.GAMES -> prefs[PS3_GAMES] = file.toString()
                        DataType.DLC -> prefs[PS3_DLC] = file.toString()
                        else -> {}
                    }
                }
                ConsoleType.PSP -> {
                    when (contentType) {
                        DataType.GAMES -> prefs[PSP_GAMES] = file.toString()
                        DataType.DLC -> prefs[PSP_DLC] = file.toString()
                        else -> {}
                    }
                }
                ConsoleType.PSX -> {
                    when (contentType) {
                        DataType.GAMES -> prefs[PSX_GAMES] = file.toString()
                        else -> {}
                    }
                }
                ConsoleType.PSM -> {
                    when (contentType) {
                        DataType.GAMES -> prefs[PSM_GAMES] = file.toString()
                        else -> {}
                    }
                }
            }
        }
    }

    suspend fun getAvailableTsvs(): List<Pair<ConsoleType, DataType>> {
        val list = mutableListOf<Pair<ConsoleType, DataType>>()
        if (psvGames.first().isNotEmpty()) list.add(Pair(ConsoleType.PSVITA, DataType.GAMES))
        if (psvDLC.first().isNotEmpty()) list.add(Pair(ConsoleType.PSVITA, DataType.DLC))
        if (psvThemes.first().isNotEmpty()) list.add(Pair(ConsoleType.PSVITA, DataType.THEMES))
        if (ps3Games.first().isNotEmpty()) list.add(Pair(ConsoleType.PS3, DataType.GAMES))
        if (ps3DLC.first().isNotEmpty()) list.add(Pair(ConsoleType.PS3, DataType.DLC))
        if (pspGames.first().isNotEmpty()) list.add(Pair(ConsoleType.PSP, DataType.GAMES))
        if (pspDLC.first().isNotEmpty()) list.add(Pair(ConsoleType.PSP, DataType.DLC))
        if (psxGames.first().isNotEmpty()) list.add(Pair(ConsoleType.PSX, DataType.GAMES))
        if (psmGames.first().isNotEmpty()) list.add(Pair(ConsoleType.PSM, DataType.GAMES))
        return list
    }

    suspend fun setHMACKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[HMAC_KEY] = key
        }
    }

    suspend fun setDownloadDir(dir: String) {
        context.dataStore.edit { prefs ->
            prefs[DOWNLOAD_DIR] = dir
        }
    }

    suspend fun setUnpackDir(dir: String) {
        context.dataStore.edit { prefs ->
            prefs[UNPACK_DIR] = dir
        }
    }

    suspend fun setUnpackInDownload(unpackInDownload: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[UNPACK_IN_DOWNLOAD] = unpackInDownload
        }
    }

    suspend fun setDeleteAfterUnpack(deleteAfterUnpack: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DELETE_AFTER_UNPACK] = deleteAfterUnpack
        }
    }

    suspend fun setPkg2zipParams(params: String) {
        context.dataStore.edit { prefs ->
            prefs[PKG2ZIP_PARAMS] = params
        }
    }

    suspend fun setPkg2zipAutoDecrypt(autoDecrypt: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PKG2ZIP_AUTO_DECRYPT] = autoDecrypt
        }
    }
}
