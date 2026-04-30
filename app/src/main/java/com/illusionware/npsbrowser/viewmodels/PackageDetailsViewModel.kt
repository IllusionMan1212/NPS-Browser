package com.illusionware.npsbrowser.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.illusionware.npsbrowser.NPSApp
import com.illusionware.npsbrowser.data.download.DownloadManager
import com.illusionware.npsbrowser.model.PackageItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SelectedPackageUiState(
    val item: PackageItem?,
)

class PackageDetailsViewModel(val downloadManager: DownloadManager): ViewModel() {
    private val _uiState = MutableStateFlow(SelectedPackageUiState(null))
    val uiState: StateFlow<SelectedPackageUiState> = _uiState.asStateFlow()

    fun setSelectedPackage(item: PackageItem) {
        _uiState.value = SelectedPackageUiState(item)
    }

    fun removeDownload(url: String) = downloadManager.cancelDownload(url)
    fun pauseDownload(url: String) = downloadManager.pauseDownload(url)
    fun resumeDownload(url: String) = downloadManager.resumeDownload(url)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as NPSApp)
                PackageDetailsViewModel(application.downloadManager)
            }
        }
    }
}