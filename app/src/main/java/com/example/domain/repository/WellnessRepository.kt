package com.example.domain.repository

import com.example.domain.model.WellnessLog
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData

interface WellnessRepository {
    val allWellnessLogs: Flow<List<WellnessLog>>
    val pagedWellnessLogs: Flow<PagingData<WellnessLog>>
    suspend fun logWellness(
        moodScore: Int,
        moodLabel: String,
        energyLevel: Int,
        hydrationCups: Int,
        gratitudeNote: String
    ): Long
    
    suspend fun incrementHydration(logId: Long)
}
