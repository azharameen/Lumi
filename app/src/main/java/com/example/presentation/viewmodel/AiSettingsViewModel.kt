package com.example.presentation.viewmodel

import com.example.domain.service.DeviceSensorsService
import com.example.domain.service.AnalyticsService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.LumiRemoteConfigManager
import com.example.data.remote.LocalLlmModelSpec
import com.example.data.remote.ModelDownloadManager
import com.example.data.remote.ModelDownloadProgress
import com.example.data.remote.ModelDownloadStatus
import com.example.domain.account.UserProfileRepository
import com.example.domain.ai.CloudModelSpec
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AiSettingsViewModel(
    val modelDownloadManager: ModelDownloadManager,
    val userProfileManager: UserProfileRepository,
    private val remoteConfigManager: LumiRemoteConfigManager? = null
) : ViewModel() {
    private val userProfile = userProfileManager.userProfile

    val localModelCatalog = modelDownloadManager.catalog
    val modelDownloadStates = modelDownloadManager.downloadStates
    val activeLocalModelId = modelDownloadManager.activeModelId
    val selectedAccelerator = modelDownloadManager.selectedAccelerator

    /** Only fully downloaded local models — shown in the model picker. */
    val downloadedLocalModels: StateFlow<List<LocalLlmModelSpec>> =
        modelDownloadManager.downloadStates.map { states ->
            modelDownloadManager.catalog.filter { spec ->
                states[spec.id]?.status == ModelDownloadStatus.DOWNLOADED
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Cloud models from Remote Config — dynamic, never hardcoded. */
    val availableCloudModels: StateFlow<List<CloudModelSpec>> =
        (remoteConfigManager?.parsedCloudModels
            ?: kotlinx.coroutines.flow.MutableStateFlow(emptyList()))
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Currently selected utility model ID from user profile. Empty = Auto. */
    val selectedUtilityModelId: StateFlow<String> = userProfile
        .map { it.selectedUtilityModelId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

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

    /** Sets the utility model and persists via user profile. */
    fun setUtilityModel(modelId: String) {
        viewModelScope.launch {
            userProfileManager.updateProfile(
                userProfile.value.copy(selectedUtilityModelId = modelId)
            )
        }
    }
}




