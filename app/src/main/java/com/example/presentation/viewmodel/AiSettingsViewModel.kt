package com.example.presentation.viewmodel
import com.example.domain.account.UserProfileManager
import com.example.data.remote.ModelDownloadManager

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AiSettingsViewModel(
    val modelDownloadManager: ModelDownloadManager,
    val userProfileManager: UserProfileManager
) : ViewModel() {
        private val userProfile = userProfileManager.userProfile
    
    val localModelCatalog = modelDownloadManager.catalog
    val modelDownloadStates = modelDownloadManager.downloadStates
    val activeLocalModelId = modelDownloadManager.activeModelId
    val selectedAccelerator = modelDownloadManager.selectedAccelerator

    fun downloadLocalModel(modelId: String) {
        modelDownloadManager.downloadModel(modelId)
    }

    fun pauseModelDownload(modelId: String) {
        modelDownloadManager.pauseDownload(modelId)
    }

    fun cancelModelDownload(modelId: String) {
        modelDownloadManager.cancelDownload(modelId)
    }

    fun deleteLocalModel(modelId: String) {
        modelDownloadManager.deleteModel(modelId)
    }

    fun setActiveLocalModel(modelId: String) {
        modelDownloadManager.setActiveModel(modelId)
    }

    fun setHardwareAccelerator(accelerator: com.example.data.remote.HardwareAccelerator) {
        modelDownloadManager.setAccelerator(accelerator)
    }

    fun updateUserProfile(profile: com.example.domain.account.UserProfileData) {
        viewModelScope.launch { userProfileManager.updateProfile(profile) }
    }
}




