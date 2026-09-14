package com.example.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.data.local.LumiDatabase
import com.example.data.local.entity.WellnessLogEntity
import com.example.data.local.mapper.toDomain
import com.example.domain.model.WellnessLog
import com.example.domain.repository.PetRepository
import com.example.domain.repository.WellnessRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WellnessRepositoryImpl(
    private val database: LumiDatabase,
    private val petRepository: PetRepository
) : WellnessRepository {
    override val allWellnessLogs: Flow<List<WellnessLog>> = 
        database.wellnessLogDao().getAllLogs().map { list -> list.map { it.toDomain() } }

    override val pagedWellnessLogs: Flow<PagingData<WellnessLog>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { database.wellnessLogDao().getPagedWellnessLogs() }
    ).flow.map { pagingData -> pagingData.map { it.toDomain() } }

    override suspend fun logWellness(
        moodScore: Int,
        moodLabel: String,
        energyLevel: Int,
        hydrationCups: Int,
        gratitudeNote: String
    ): Long {
        val id = database.wellnessLogDao().insertLog(
            WellnessLogEntity(
                moodScore = moodScore,
                moodLabel = moodLabel,
                energyLevel = energyLevel,
                hydrationCups = hydrationCups,
                gratitudeNote = gratitudeNote
            )
        )
        petRepository.earnCoinsAndExp(coins = 15, exp = 15, reason = "Wellness Reflection")
        return id
    }

    override suspend fun incrementHydration(logId: Long) {
        database.wellnessLogDao().incrementHydration(logId)
        petRepository.setSpeechBubbleText("Great job hydrating! 💧 Stay energized!")
    }
}
