package com.illusionware.npsbrowser.viewmodels

import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.illusionware.npsbrowser.NPSApp
import com.illusionware.npsbrowser.data.ItemLayout
import com.illusionware.npsbrowser.data.SettingsPreferences
import com.illusionware.npsbrowser.data.SettingsPreferencesRepository
import com.illusionware.npsbrowser.data.Theme
import com.illusionware.npsbrowser.model.ConsoleType
import com.illusionware.npsbrowser.model.PackageItemType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsPrefsRepo: SettingsPreferencesRepository): ViewModel() {
    val uiState: StateFlow<SettingsPreferences> =
        settingsPrefsRepo.getPreferences().stateIn(viewModelScope, SharingStarted.Lazily, SettingsPreferences(
            Theme.SYSTEM.ordinal,
            ItemLayout.LIST.ordinal,
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            false,
            false,
            "",
            true,
        ))

    fun availableTsvs(): MutableList<Pair<ConsoleType, PackageItemType>> {
        val list = mutableListOf<Pair<ConsoleType, PackageItemType>>()
        if (uiState.value.psvGames.isNotEmpty()) list.add(Pair(ConsoleType.PSVITA, PackageItemType.GAME))
        if (uiState.value.ps3Games.isNotEmpty()) list.add(Pair(ConsoleType.PS3, PackageItemType.GAME))
        if (uiState.value.pspGames.isNotEmpty()) list.add(Pair(ConsoleType.PSP, PackageItemType.GAME))
        if (uiState.value.psxGames.isNotEmpty()) list.add(Pair(ConsoleType.PSX, PackageItemType.GAME))
        if (uiState.value.psmGames.isNotEmpty()) list.add(Pair(ConsoleType.PSM, PackageItemType.GAME))

        return list
    }

    suspend fun getAppTheme(): Int {
        return settingsPrefsRepo.getPreferences().first().appTheme
    }

    fun shouldCreateDefaultDir(): Boolean {
        val defaultDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/NPSBrowser"

        return uiState.value.downloadDir == defaultDir
                || uiState.value.unpackDir == defaultDir
    }

    fun setTSVFile(console: ConsoleType, itemType: PackageItemType, file: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsPrefsRepo.setTSVFile(console, itemType, file)
        }
    }

    fun setTheme(theme: Theme) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsPrefsRepo.setTheme(theme)
        }
    }

    fun setLayout(layout: ItemLayout) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsPrefsRepo.setLayout(layout)
        }
    }

    fun setHMACKey(hmacKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsPrefsRepo.setHMACKey(hmacKey)
        }
    }

    fun setDownloadDir(downloadDir: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsPrefsRepo.setDownloadDir(downloadDir)
        }
    }

    fun setUnpackDir(unpackDir: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsPrefsRepo.setUnpackDir(unpackDir)
        }
    }

    fun setUnpackInDownload(unpackInDownload: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsPrefsRepo.setUnpackInDownload(unpackInDownload)
        }
    }

    fun setDeleteAfterUnpack(deleteAfterUnpack: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsPrefsRepo.setDeleteAfterUnpack(deleteAfterUnpack)
        }
    }

    fun setPkg2zipParams(pkg2zipParams: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsPrefsRepo.setPkg2zipParams(pkg2zipParams)
        }
    }

    fun setPkg2zipAutoDecrypt(pkg2zipAutoDecrypt: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsPrefsRepo.setPkg2zipAutoDecrypt(pkg2zipAutoDecrypt)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as NPSApp)
                SettingsViewModel(application.settingsPreferencesRepository)
            }
        }
    }
}