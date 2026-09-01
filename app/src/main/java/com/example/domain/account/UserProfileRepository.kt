package com.example.domain.account

import kotlinx.coroutines.flow.StateFlow

interface UserProfileRepository {
    val userProfile: StateFlow<UserProfileData>
    val userFacts: StateFlow<List<UserFactItem>>
    
    fun updateProfile(profile: UserProfileData)
    fun updateField(block: (UserProfileData) -> UserProfileData)
    fun addUserFact(category: String, factText: String, isPinned: Boolean = false)
    fun removeUserFact(id: String)
    fun togglePinFact(id: String)
    fun resetToDefaults()
}
