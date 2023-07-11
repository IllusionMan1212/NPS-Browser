package com.illusionware.npsbrowser.data

import android.net.Uri
import android.os.Environment
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.illusionware.npsbrowser.model.ConsoleType
import com.illusionware.npsbrowser.model.PackageItemType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val defaultDownloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/NPSBrowser"

enum class ItemLayout {
    LIST,
    GRID,
}

enum class Theme {
    LIGHT,
    DARK,
    SYSTEM,
}

data class SettingsPreferences(
    val appTheme: Int,
    val layout: Int,
    val psvGames: String,
    val ps3Games: String,
    val pspGames: String,
    val psxGames: String,
    val psmGames: String,
    val psvDlc: String,
    val ps3Dlc: String,
    val pspDlc: String,
    val psvThemes: String,
    val hmacKey: String,
    val downloadDir: String,
    val unpackDir: String,
    val unpackInDownload: Boolean,
    val deleteAfterUnpack: Boolean,
    val pkg2zipParams: String,
    val pkg2zipAutoDecrypt: Boolean,
)

class SettingsPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val APP_THEME = intPreferencesKey("app_theme")
        private val LAYOUT = intPreferencesKey("layout")
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

    private val tsvFiles = MutableSharedFlow<Triple<String, ConsoleType, PackageItemType>>()

    fun getPreferences(): Flow<SettingsPreferences> =
        dataStore.data.map { mapSettingsPreferences(it) }

    suspend fun availableTsvs(): MutableList<Triple<String, ConsoleType, PackageItemType>> {
         val list = mutableListOf<Triple<String, ConsoleType, PackageItemType>>()

        return dataStore.data.map {
            if (it[PSV_GAMES]?.isNotEmpty() == true) list.add(Triple(it[PSV_GAMES] ?: "", ConsoleType.PSVITA, PackageItemType.GAME))
            if (it[PSV_DLC]?.isNotEmpty() == true) list.add(Triple(it[PSV_DLC] ?: "", ConsoleType.PSVITA, PackageItemType.DLC))
            if (it[PSV_THEMES]?.isNotEmpty() == true) list.add(Triple(it[PSV_THEMES] ?: "", ConsoleType.PSVITA, PackageItemType.THEME))
            if (it[PS3_GAMES]?.isNotEmpty() == true) list.add(Triple(it[PS3_GAMES] ?: "", ConsoleType.PS3, PackageItemType.GAME))
            if (it[PS3_DLC]?.isNotEmpty() == true) list.add(Triple(it[PS3_DLC] ?: "", ConsoleType.PS3, PackageItemType.DLC))
            if (it[PSP_GAMES]?.isNotEmpty() == true) list.add(Triple(it[PSP_GAMES] ?: "", ConsoleType.PSP, PackageItemType.GAME))
            if (it[PSP_DLC]?.isNotEmpty() == true) list.add(Triple(it[PSP_DLC] ?: "", ConsoleType.PSP, PackageItemType.DLC))
            if (it[PSX_GAMES]?.isNotEmpty() == true) list.add(Triple(it[PSX_GAMES] ?: "", ConsoleType.PSX, PackageItemType.GAME))
            if (it[PSM_GAMES]?.isNotEmpty() == true) list.add(Triple(it[PSM_GAMES] ?: "", ConsoleType.PSM, PackageItemType.GAME))

            list
        }.first()
    }

    suspend fun setTheme(theme: Theme) {
        dataStore.edit { prefs ->
            prefs[APP_THEME] = theme.ordinal
        }
    }

    suspend fun setLayout(layout: ItemLayout) {
        dataStore.edit { prefs ->
            prefs[LAYOUT] = layout.ordinal
        }
    }

    suspend fun setHMACKey(key: String) {
        dataStore.edit { prefs ->
            prefs[HMAC_KEY] = key
        }
    }

    suspend fun setDownloadDir(dir: String) {
        dataStore.edit { prefs ->
            prefs[DOWNLOAD_DIR] = dir
        }
    }

    suspend fun setUnpackDir(dir: String) {
        dataStore.edit { prefs ->
            prefs[UNPACK_DIR] = dir
        }
    }

    suspend fun setUnpackInDownload(unpackInDownload: Boolean) {
        dataStore.edit { prefs ->
            prefs[UNPACK_IN_DOWNLOAD] = unpackInDownload
        }
    }

    suspend fun setDeleteAfterUnpack(deleteAfterUnpack: Boolean) {
        dataStore.edit { prefs ->
            prefs[DELETE_AFTER_UNPACK] = deleteAfterUnpack
        }
    }

    suspend fun setPkg2zipParams(params: String) {
        dataStore.edit { prefs ->
            prefs[PKG2ZIP_PARAMS] = params
        }
    }

    suspend fun setPkg2zipAutoDecrypt(autoDecrypt: Boolean) {
        dataStore.edit { prefs ->
            prefs[PKG2ZIP_AUTO_DECRYPT] = autoDecrypt
        }
    }

    fun tsvFiles() = tsvFiles

    suspend fun setTSVFile(console: ConsoleType, contentType: PackageItemType, file: Uri) {
        tsvFiles.emit(Triple(file.toString(), console, contentType))

        dataStore.edit { prefs ->
            when (console) {
                ConsoleType.PSVITA -> {
                    when (contentType) {
                        PackageItemType.GAME -> prefs[PSV_GAMES] = file.toString()
                        PackageItemType.DLC -> prefs[PSV_DLC] = file.toString()
                        PackageItemType.THEME -> prefs[PSV_THEMES] = file.toString()
                    }
                }
                ConsoleType.PS3 -> {
                    when (contentType) {
                        PackageItemType.GAME -> prefs[PS3_GAMES] = file.toString()
                        PackageItemType.DLC -> prefs[PS3_DLC] = file.toString()
                        else -> {}
                    }
                }
                ConsoleType.PSP -> {
                    when (contentType) {
                        PackageItemType.GAME -> prefs[PSP_GAMES] = file.toString()
                        PackageItemType.DLC -> prefs[PSP_DLC] = file.toString()
                        else -> {}
                    }
                }
                ConsoleType.PSX -> {
                    when (contentType) {
                        PackageItemType.GAME -> prefs[PSX_GAMES] = file.toString()
                        else -> {}
                    }
                }
                ConsoleType.PSM -> {
                    when (contentType) {
                        PackageItemType.GAME -> prefs[PSM_GAMES] = file.toString()
                        else -> {}
                    }
                }
            }
        }
    }

    private fun mapSettingsPreferences(prefs: Preferences): SettingsPreferences {
        return SettingsPreferences(
            prefs[APP_THEME] ?: Theme.SYSTEM.ordinal,
            prefs[LAYOUT] ?: ItemLayout.LIST.ordinal,
            prefs[PSV_GAMES] ?: "",
            prefs[PS3_GAMES] ?: "",
            prefs[PSP_GAMES] ?: "",
            prefs[PSX_GAMES] ?: "",
            prefs[PSM_GAMES] ?: "",
            prefs[PSV_DLC] ?: "",
            prefs[PS3_DLC] ?: "",
            prefs[PSP_DLC] ?: "",
            prefs[PSV_THEMES] ?: "",
            prefs[HMAC_KEY] ?: "",
            prefs[DOWNLOAD_DIR] ?: defaultDownloadDir,
            prefs[UNPACK_DIR] ?: defaultDownloadDir,
            prefs[UNPACK_IN_DOWNLOAD] ?: false,
            prefs[DELETE_AFTER_UNPACK] ?: false,
            prefs[PKG2ZIP_PARAMS] ?: "-x {pkgFile} \"{zRifKey}\"",
            prefs[PKG2ZIP_AUTO_DECRYPT] ?: true,
        )
    }
}