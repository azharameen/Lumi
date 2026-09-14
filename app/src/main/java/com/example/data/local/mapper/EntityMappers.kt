package com.example.data.local.mapper

import com.example.data.local.entity.*
import com.example.domain.model.*

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    notes = notes,
    dueDate = dueDate,
    priority = priority,
    isCompleted = isCompleted,
    category = category,
    estimatedMinutes = estimatedMinutes,
    createdAt = createdAt
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    notes = notes,
    dueDate = dueDate,
    priority = priority,
    isCompleted = isCompleted,
    category = category,
    estimatedMinutes = estimatedMinutes,
    createdAt = createdAt
)

fun ChatMessageEntity.toDomain(): ChatMessage = ChatMessage(
    id = id,
    timestamp = timestamp,
    sender = sender,
    content = content,
    petEmotion = petEmotion,
    toolUsedName = toolUsedName,
    toolResultJson = toolResultJson,
    imageBase64OrUri = imageBase64OrUri
)

fun ChatMessage.toEntity(): ChatMessageEntity = ChatMessageEntity(
    id = id,
    timestamp = timestamp,
    sender = sender,
    content = content,
    petEmotion = petEmotion,
    toolUsedName = toolUsedName,
    toolResultJson = toolResultJson,
    imageBase64OrUri = imageBase64OrUri
)

fun CalendarEventEntity.toDomain(): CalendarEvent = CalendarEvent(
    id = id,
    title = title,
    description = description,
    startTimeMillis = startTimeMillis,
    endTimeMillis = endTimeMillis,
    location = location,
    isAllDay = isAllDay,
    category = category,
    colorHex = colorHex,
    reminderMinutesBefore = reminderMinutesBefore,
    createdAt = createdAt
)

fun CalendarEvent.toEntity(): CalendarEventEntity = CalendarEventEntity(
    id = id,
    title = title,
    description = description,
    startTimeMillis = startTimeMillis,
    endTimeMillis = endTimeMillis,
    location = location,
    isAllDay = isAllDay,
    category = category,
    colorHex = colorHex,
    reminderMinutesBefore = reminderMinutesBefore,
    createdAt = createdAt
)

fun WellnessLogEntity.toDomain(): WellnessLog = WellnessLog(
    id = id,
    timestamp = timestamp,
    moodScore = moodScore,
    moodLabel = moodLabel,
    energyLevel = energyLevel,
    hydrationCups = hydrationCups,
    sleepHours = sleepHours,
    gratitudeNote = gratitudeNote,
    stressLevel = stressLevel,
    breathingMinutesCompleted = breathingMinutesCompleted
)

fun WellnessLog.toEntity(): WellnessLogEntity = WellnessLogEntity(
    id = id,
    timestamp = timestamp,
    moodScore = moodScore,
    moodLabel = moodLabel,
    energyLevel = energyLevel,
    hydrationCups = hydrationCups,
    sleepHours = sleepHours,
    gratitudeNote = gratitudeNote,
    stressLevel = stressLevel,
    breathingMinutesCompleted = breathingMinutesCompleted
)

fun GoalPlanEntity.toDomain(): GoalPlan = GoalPlan(
    id = id,
    title = title,
    description = description,
    category = category,
    targetDate = targetDate,
    status = status,
    totalSteps = totalSteps,
    completedSteps = completedSteps,
    createdAt = createdAt,
    isAiGenerated = isAiGenerated,
    tagsJson = tagsJson
)

fun GoalPlan.toEntity(): GoalPlanEntity = GoalPlanEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    targetDate = targetDate,
    status = status,
    totalSteps = totalSteps,
    completedSteps = completedSteps,
    createdAt = createdAt,
    isAiGenerated = isAiGenerated,
    tagsJson = tagsJson
)

fun GoalMilestoneEntity.toDomain(): GoalMilestone = GoalMilestone(
    id = id,
    goalId = goalId,
    phaseNumber = phaseNumber,
    phaseTitle = phaseTitle,
    stepTitle = stepTitle,
    stepDescription = stepDescription,
    suggestedTool = suggestedTool,
    isCompleted = isCompleted,
    executionOutput = executionOutput,
    scheduledDate = scheduledDate
)

fun GoalMilestone.toEntity(): GoalMilestoneEntity = GoalMilestoneEntity(
    id = id,
    goalId = goalId,
    phaseNumber = phaseNumber,
    phaseTitle = phaseTitle,
    stepTitle = stepTitle,
    stepDescription = stepDescription,
    suggestedTool = suggestedTool,
    isCompleted = isCompleted,
    executionOutput = executionOutput,
    scheduledDate = scheduledDate
)

fun PetMemoryEntity.toDomain(): PetMemory = PetMemory(
    id = id,
    timestamp = timestamp,
    category = category,
    memoryText = memoryText,
    sentiment = sentiment,
    emotionalImpact = emotionalImpact,
    isPinned = isPinned
)

fun PetMemory.toEntity(): PetMemoryEntity = PetMemoryEntity(
    id = id,
    timestamp = timestamp,
    category = category,
    memoryText = memoryText,
    sentiment = sentiment,
    emotionalImpact = emotionalImpact,
    isPinned = isPinned
)

fun FactKnowledgeEntity.toDomain(): UserFact = UserFact(
    id = id.toString(),
    factKey = predicate,
    factValue = objectValue,
    isPinned = false,
    createdAt = lastUpdatedMillis
)

fun UserFact.toEntity(): FactKnowledgeEntity = FactKnowledgeEntity(
    id = id.toLongOrNull() ?: 0L,
    predicate = factKey,
    objectValue = factValue,
    lastUpdatedMillis = createdAt
)

fun AiExecutionLogEntity.toDomain(): AiExecutionLog = AiExecutionLog(
    id = id,
    taskCategory = taskCategory,
    engineType = engineType,
    modelName = modelName,
    promptPreview = promptPreview,
    responsePreview = responsePreview,
    promptTokens = promptTokens,
    completionTokens = completionTokens,
    totalTokens = totalTokens,
    estimatedCostUsd = estimatedCostUsd,
    startTimeMillis = startTimeMillis,
    finishTimeMillis = finishTimeMillis,
    durationMs = durationMs,
    isSuccess = isSuccess,
    errorMessage = errorMessage,
    isOffline = isOffline,
    hardwareTarget = hardwareTarget,
    routingReason = routingReason,
    fallbackTriggered = fallbackTriggered
)

fun AiExecutionLog.toEntity(): AiExecutionLogEntity = AiExecutionLogEntity(
    id = id,
    taskCategory = taskCategory,
    engineType = engineType,
    modelName = modelName,
    promptPreview = promptPreview,
    responsePreview = responsePreview,
    promptTokens = promptTokens,
    completionTokens = completionTokens,
    totalTokens = totalTokens,
    estimatedCostUsd = estimatedCostUsd,
    startTimeMillis = startTimeMillis,
    finishTimeMillis = finishTimeMillis,
    durationMs = durationMs,
    isSuccess = isSuccess,
    errorMessage = errorMessage,
    isOffline = isOffline,
    hardwareTarget = hardwareTarget,
    routingReason = routingReason,
    fallbackTriggered = fallbackTriggered
)
