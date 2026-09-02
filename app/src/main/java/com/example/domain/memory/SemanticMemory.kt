package com.example.domain.memory

import com.example.data.local.LumiDatabase
import com.example.data.local.entity.FactKnowledgeEntity

/**
 * 3. Semantic Memory Tier:
 * User facts & profile knowledge graph triples (`FactKnowledgeEntity`).
 */
class SemanticMemory(private val database: LumiDatabase) {

    suspend fun saveFact(subject: String, predicate: String, objectValue: String, confidence: Float = 0.95f): Long {
        val fact = FactKnowledgeEntity(
            subject = subject,
            predicate = predicate,
            objectValue = objectValue,
            confidence = confidence,
            lastUpdatedMillis = System.currentTimeMillis()
        )
        return database.factKnowledgeDao().insertOrUpdateFact(fact)
    }

    suspend fun getAllFacts(): List<FactKnowledgeEntity> {
        return database.factKnowledgeDao().getAllFactsDirect()
    }
}
