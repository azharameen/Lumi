package com.example.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.LumiAnalyticsManager
import com.example.data.firebase.LumiRemoteConfigManager
import com.example.domain.model.LumiRemoteConfig
import com.example.data.local.entity.*
import com.example.data.repository.LumiRepositoryImpl
import com.example.domain.briefing.AutonomousBriefingEngine
import com.example.domain.briefing.BriefingType
import com.example.domain.briefing.DailyBriefing
import com.example.data.device.SoundscapeType
import com.example.domain.usecase.goal.DecomposeGoalUseCase
import com.example.domain.repository.TaskGoalRepository
import com.example.domain.repository.LumiRepository
import com.example.data.device.*
import com.example.data.firebase.*
import com.example.data.local.entity.*
import com.example.domain.briefing.*
import com.example.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class LifeHubViewModel(
    val taskGoalRepository: TaskGoalRepository,
    val decomposeGoalUseCase: DecomposeGoalUseCase,
    val repository: LumiRepository, // Still needed for Soundscape for now
    val sensorsManager: SensorsManager,
    val briefingEngine: AutonomousBriefingEngine,
    val remoteConfigManager: LumiRemoteConfigManager? = null,
    val analytics: LumiAnalyticsManager? = null
) : ViewModel() {
    val allTasks: StateFlow<List<TaskEntity>> = taskGoalRepository.allTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCalendarEvents: StateFlow<List<CalendarEventEntity>> = taskGoalRepository.allCalendarEvents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allGoalPlans: StateFlow<List<GoalPlanEntity>> = taskGoalRepository.allGoalPlans.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val soundscapeState = repository.soundscapeState

    val remoteConfig: StateFlow<LumiRemoteConfig> = remoteConfigManager?.config ?: MutableStateFlow(LumiRemoteConfig())

    private val _dailyBriefing = MutableStateFlow<DailyBriefing?>(null)
    val dailyBriefing: StateFlow<DailyBriefing?> = _dailyBriefing.asStateFlow()
    
    private val _isBriefingGenerating = MutableStateFlow(false)
    val isBriefingGenerating: StateFlow<Boolean> = _isBriefingGenerating.asStateFlow()

    fun decomposeGoal(title: String, description: String, category: String = "Productivity", targetDate: String = "") {
        viewModelScope.launch {
            sensorsManager.vibrateCelebration()
            analytics?.logGoalMilestone(title, category, isCompleted = false)
            decomposeGoalUseCase(title, description, category, targetDate)
        }
    }

    fun executeMilestone(milestoneId: Long, goalId: Long) {
        viewModelScope.launch {
            sensorsManager.vibrateTap()
            taskGoalRepository.executeMilestoneTool(milestoneId, goalId)
        }
    }

    fun toggleMilestone(milestoneId: Long, goalId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            sensorsManager.vibrateTap()
            if (isCompleted) {
                analytics?.logGoalMilestone("Milestone #$milestoneId", "Goal", isCompleted = true)
            }
            taskGoalRepository.toggleMilestone(milestoneId, goalId, isCompleted)
        }
    }

    fun deleteGoal(goalId: Long) { viewModelScope.launch { taskGoalRepository.deleteGoal(goalId) } }
    
    fun toggleTask(taskId: Long, isCompleted: Boolean) { viewModelScope.launch { taskGoalRepository.toggleTaskCompleted(taskId, isCompleted) } }
    
    fun deleteTask(task: TaskEntity) { viewModelScope.launch { taskGoalRepository.deleteTask(task) } }
    
    fun addTask(title: String, priority: String, category: String, estimatedMinutes: Int, notes: String) {
        viewModelScope.launch { taskGoalRepository.addTask(title, priority, category, estimatedMinutes, notes) }
    }

    fun addCalendarEvent(event: CalendarEventEntity) { viewModelScope.launch { taskGoalRepository.addCalendarEvent(event) } }
    fun deleteCalendarEvent(id: Long) { viewModelScope.launch { taskGoalRepository.deleteCalendarEvent(id) } }

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
    
    fun getMilestonesForGoal(goalId: Long) = taskGoalRepository.getMilestonesForGoal(goalId)
}



