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

    companion object {
        @Volatile
        private var INSTANCE: LumiDatabase? = null

        fun getDatabase(context: Context): LumiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LumiDatabase::class.java,
                    "lumi_ai_companion.db"
                ).fallbackToDestructiveMigration(true).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
