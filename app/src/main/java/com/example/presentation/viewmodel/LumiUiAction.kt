package com.example.presentation.viewmodel
import com.example.domain.account.UserProfileManager
import com.example.data.remote.ModelDownloadManager

import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.device.SoundscapeType

sealed interface LumiUiAction {
    // Navigation
    data class NavigateToChat(val prompt: String? = null) : LumiUiAction
    data class SetLifeHubSubTab(val tabIndex: Int) : LumiUiAction
    
    // Calendar
    data class AddCalendarEvent(val event: CalendarEventEntity) : LumiUiAction
    data class DeleteCalendarEvent(val id: Long) : LumiUiAction
    
    // Briefing
    object SpeakBriefing : LumiUiAction
    
    // Tasks
    data class AddTask(val title: String, val priority: String, val category: String, val estimatedMinutes: Int, val notes: String) : LumiUiAction
    data class ToggleTask(val id: Long, val isCompleted: Boolean) : LumiUiAction
    data class DeleteTask(val task: TaskEntity) : LumiUiAction
    
    // Goals
    data class DecomposeGoal(val title: String, val description: String, val category: String, val deadline: String) : LumiUiAction
    data class DeleteGoal(val id: Long) : LumiUiAction
    data class ToggleMilestone(val milestoneId: Long, val goalId: Long, val isCompleted: Boolean) : LumiUiAction
    data class ExecuteMilestone(val milestoneId: Long, val goalId: Long) : LumiUiAction
    
    // Soundscapes & Focus
    data class StartSoundscape(val type: SoundscapeType) : LumiUiAction
    object StopSoundscape : LumiUiAction
    data class SetSoundscapeVolume(val volume: Float) : LumiUiAction
    data class StartFocusTimer(val minutes: Int) : LumiUiAction
    object StopFocusTimer : LumiUiAction
}

