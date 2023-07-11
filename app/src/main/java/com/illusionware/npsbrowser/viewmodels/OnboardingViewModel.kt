package com.illusionware.npsbrowser.viewmodels

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.illusionware.npsbrowser.NPSApp
import com.illusionware.npsbrowser.data.OnboardingPreferences
import com.illusionware.npsbrowser.data.OnboardingPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class OnboardingViewModel(
    private val onboardingPreferencesRepository: OnboardingPreferencesRepository
): ViewModel() {
    val uiState: StateFlow<OnboardingPreferences> =
        onboardingPreferencesRepository.getPreferences().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(2_000),
            OnboardingPreferences(
                false,
            )
        )

    fun finishOnboarding(shouldCreateDefaultDir: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (shouldCreateDefaultDir) {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "NPSBrowser")
                if (!dir.exists()) {
                    dir.mkdir()
                }
            }
            onboardingPreferencesRepository.setSeenOnboarding(true)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as NPSApp)
                OnboardingViewModel(application.onboardingPreferencesRepository)
            }
        }
    }
}