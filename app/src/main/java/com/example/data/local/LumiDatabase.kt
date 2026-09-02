package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import com.example.data.local.entity.ToolFtsEntity
import com.example.data.local.dao.ToolFtsDao

@Database(
    entities = [
        TaskEntity::class,
        CalendarEventEntity::class,
        WellnessLogEntity::class,
        PetEvolutionEntity::class,
        PetMemoryEntity::class,
        ChatMessageEntity::class,
        AiExecutionLogEntity::class,
        GoalPlanEntity::class,
        GoalMilestoneEntity::class,
        FactKnowledgeEntity::class,
        AgentCheckpointEntity::class,
        ToolFtsEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class LumiDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun wellnessLogDao(): WellnessLogDao
    abstract fun petEvolutionDao(): PetEvolutionDao
    abstract fun petMemoryDao(): PetMemoryDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun aiExecutionLogDao(): AiExecutionLogDao
    abstract fun goalPlanDao(): GoalPlanDao
    abstract fun factKnowledgeDao(): FactKnowledgeDao
    abstract fun agentCheckpointDao(): AgentCheckpointDao
    abstract fun toolFtsDao(): ToolFtsDao

    /**
     * Performs a deep SQLite integrity check and basic table sanity queries.
     * Returns true if the database is healthy.
     */
    suspend fun performHealthCheck(): Boolean {
        return try {
            val cursor = query("PRAGMA integrity_check", null)
            val result = if (cursor.moveToFirst()) cursor.getString(0) else "failed"
            cursor.close()
            
            // Result should be "ok" if SQLite structure is valid
            if (result.lowercase() != "ok") return false
            
            // Check if vital table is reachable
            petEvolutionDao().getPetCount()
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: LumiDatabase? = null

        fun getDatabase(context: Context): LumiDatabase {
            return INSTANCE ?: synchronized(this) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    LumiDatabase::class.java,
                    "lumi_ai_companion.db"
                )
                
                if (com.example.BuildConfig.DEBUG) {
                    builder.fallbackToDestructiveMigration(dropAllTables = true)
                } else {
                    builder.fallbackToDestructiveMigrationOnDowngrade()
                }

                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }
    }
}
