package com.example.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.*
import com.example.data.repository.LumiRepositoryImpl
import com.example.domain.briefing.AutonomousBriefingEngine
import com.example.domain.briefing.BriefingType
import com.example.domain.briefing.DailyBriefing
import com.example.data.device.SoundscapeType
import com.example.domain.repository.LumiRepository
import com.example.data.device.SensorsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LifeHubViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as com.example.LumiApplication).container
    val repository: LumiRepository = container.repository
    val sensorsManager = container.sensorsManager
    val briefingEngine = container.briefingEngine

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCalendarEvents: StateFlow<List<CalendarEventEntity>> = repository.allCalendarEvents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allGoalPlans: StateFlow<List<GoalPlanEntity>> = repository.allGoalPlans.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val soundscapeState = repository.soundscapeState

    private val _dailyBriefing = MutableStateFlow<DailyBriefing?>(null)
    val dailyBriefing: StateFlow<DailyBriefing?> = _dailyBriefing.asStateFlow()
    
    private val _isBriefingGenerating = MutableStateFlow(false)
    val isBriefingGenerating: StateFlow<Boolean> = _isBriefingGenerating.asStateFlow()

    fun decomposeGoal(title: String, description: String, category: String = "Productivity", targetDate: String = "") {
        viewModelScope.launch {
            sensorsManager.vibrateCelebration()
            repository.decomposeGoal(title, description, category, targetDate)
        }
    }

    fun executeMilestone(milestoneId: Long, goalId: Long) {
        viewModelScope.launch {
            sensorsManager.vibrateTap()
            repository.executeMilestoneTool(milestoneId, goalId)
        }
    }

    fun toggleMilestone(milestoneId: Long, goalId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            sensorsManager.vibrateTap()
            repository.toggleMilestone(milestoneId, goalId, isCompleted)
        }
    }

    fun deleteGoal(goalId: Long) { viewModelScope.launch { repository.deleteGoal(goalId) } }
    
    fun toggleTask(taskId: Long, isCompleted: Boolean) { viewModelScope.launch { repository.toggleTaskCompleted(taskId, isCompleted) } }
    
    fun deleteTask(task: TaskEntity) { viewModelScope.launch { repository.deleteTask(task) } }
    
    fun addTask(title: String, priority: String, category: String, estimatedMinutes: Int, notes: String) {
        viewModelScope.launch { repository.addTask(title, priority, category, estimatedMinutes, notes) }
    }

    fun addCalendarEvent(event: CalendarEventEntity) { viewModelScope.launch { repository.addCalendarEvent(event) } }
    fun deleteCalendarEvent(id: Long) { viewModelScope.launch { repository.deleteCalendarEvent(id) } }

    fun startSoundscape(type: SoundscapeType) {
        sensorsManager.vibrateTap()
        repository.startSoundscape(type)
    }

    fun stopSoundscape() { repository.stopSoundscape() }
    fun setSoundscapeVolume(volume: Float) { repository.setSoundscapeVolume(volume) }

    fun startFocusTimerWithSoundscape(minutes: Int) {
        sensorsManager.vibrateCelebration()
        repository.startFocusTimerWithSoundscape(minutes)
    }
    fun stopFocusTimerWithSoundscape() { repository.stopFocusTimerWithSoundscape() }

    fun refreshDailyBriefing(
        type: BriefingType? = null,
        petStatus: com.example.domain.model.PetStatus,
        petEvolution: com.example.data.local.entity.PetEvolutionEntity?,
        wellnessLogs: List<WellnessLogEntity>
    ) {
        viewModelScope.launch {
            _isBriefingGenerating.value = true
            val briefing = briefingEngine.generateBriefing(
                type = type ?: BriefingType.MORNING,
                petStatus = petStatus,
                petEvolution = petEvolution,
                tasks = allTasks.value,
                events = allCalendarEvents.value,
                wellnessLogs = wellnessLogs
            )
            _dailyBriefing.value = briefing
            _isBriefingGenerating.value = false
        }
    }
    
    fun getMilestonesForGoal(goalId: Long) = repository.getMilestonesForGoal(goalId)
}
