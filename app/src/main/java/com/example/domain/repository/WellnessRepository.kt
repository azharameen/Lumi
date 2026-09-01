package com.example.domain.repository

import com.example.data.local.entity.WellnessLogEntity
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData

interface WellnessRepository {
    val allWellnessLogs: Flow<List<WellnessLogEntity>>
    val pagedWellnessLogs: Flow<PagingData<WellnessLogEntity>>

    suspend fun logWellness(
        moodScore: Int,
        moodLabel: String,
        energyLevel: Int,
        hydrationCups: Int,
        gratitudeNote: String
    ): Long
    
    suspend fun incrementHydration(logId: Long)
}
