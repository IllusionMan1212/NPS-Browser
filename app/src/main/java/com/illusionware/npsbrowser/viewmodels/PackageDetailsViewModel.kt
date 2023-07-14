package com.illusionware.npsbrowser.viewmodels

import androidx.lifecycle.ViewModel
import com.illusionware.npsbrowser.model.PackageItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SelectedPackageUiState(
    val item: PackageItem?,
)

class PackageDetailsViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(SelectedPackageUiState(null))
    val uiState: StateFlow<SelectedPackageUiState> = _uiState.asStateFlow()

    fun setSelectedPackage(item: PackageItem) {
        _uiState.value = SelectedPackageUiState(item)
    }
}