package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AiExecutionLogDao
import com.example.data.local.dao.CalendarEventDao
import com.example.data.local.dao.ChatMessageDao
import com.example.data.local.dao.GoalPlanDao
import com.example.data.local.dao.PetEvolutionDao
import com.example.data.local.dao.PetMemoryDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.dao.WellnessLogDao
import com.example.data.local.entity.AiExecutionLogEntity
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.GoalMilestoneEntity
import com.example.data.local.entity.GoalPlanEntity
import com.example.data.local.entity.PetEvolutionEntity
import com.example.data.local.entity.PetMemoryEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.WellnessLogEntity

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
        GoalMilestoneEntity::class
    ],
    version = 4,
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

    companion object {
        @Volatile
        private var INSTANCE: LumiDatabase? = null

        fun getDatabase(context: Context): LumiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LumiDatabase::class.java,
                    "lumi_ai_companion.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
