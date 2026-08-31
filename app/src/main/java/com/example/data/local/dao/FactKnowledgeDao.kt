package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.FactKnowledgeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FactKnowledgeDao {
    @Query("SELECT * FROM fact_knowledge_graph ORDER BY lastUpdatedMillis DESC")
    fun getAllFacts(): Flow<List<FactKnowledgeEntity>>

    @Query("SELECT * FROM fact_knowledge_graph ORDER BY lastUpdatedMillis DESC")
    suspend fun getAllFactsDirect(): List<FactKnowledgeEntity>

    @Query("SELECT * FROM fact_knowledge_graph WHERE predicate = :predicate LIMIT 1")
    suspend fun getFactByPredicate(predicate: String): FactKnowledgeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateFact(fact: FactKnowledgeEntity): Long

    @Query("DELETE FROM fact_knowledge_graph WHERE id = :id")
    suspend fun deleteFact(id: Long)
}
