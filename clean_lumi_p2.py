with open("app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt", "r") as f:
    content = f.read()

funcs = [
    "val petStatus = repository.petStatus.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PetStatus())",
    "val petEvolution = repository.petEvolution.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)",
    "val allTasks = repository.allTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())",
    "val allCalendarEvents = repository.allCalendarEvents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())",
    "val allGoalPlans = repository.allGoalPlans.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())",
    "val soundscapeState = repository.soundscapeState",
    "private val _dailyBriefing = MutableStateFlow<DailyBriefing?>(null)",
    "val dailyBriefing = _dailyBriefing.asStateFlow()",
    "private val _isBriefingGenerating = MutableStateFlow(false)",
    "val isBriefingGenerating = _isBriefingGenerating.asStateFlow()",
    """    fun decomposeGoal(title: String, description: String, category: String = "Productivity", targetDate: String = "") {
        viewModelScope.launch {
            sensorsManager.vibrateCelebration()
            repository.decomposeGoal(title, description, category, targetDate)
        }
    }""",
    """    fun executeMilestone(milestoneId: Long, goalId: Long) {
        viewModelScope.launch {
            sensorsManager.vibrateTap()
            repository.executeMilestoneTool(milestoneId, goalId)
        }
    }""",
    """    fun toggleMilestone(milestoneId: Long, goalId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            sensorsManager.vibrateTap()
            repository.toggleMilestone(milestoneId, goalId, isCompleted)
        }
    }""",
    "fun deleteGoal(goalId: Long) { viewModelScope.launch { repository.deleteGoal(goalId) } }",
    "fun toggleTask(taskId: Long, isCompleted: Boolean) { viewModelScope.launch { repository.toggleTaskCompleted(taskId, isCompleted) } }",
    "fun deleteTask(task: TaskEntity) { viewModelScope.launch { repository.deleteTask(task) } }",
    """    fun addTask(title: String, priority: String, category: String, estimatedMinutes: Int, notes: String) {
        viewModelScope.launch { repository.addTask(title, priority, category, estimatedMinutes, notes) }
    }""",
    "fun addCalendarEvent(event: CalendarEventEntity) { viewModelScope.launch { repository.addCalendarEvent(event) } }",
    "fun deleteCalendarEvent(id: Long) { viewModelScope.launch { repository.deleteCalendarEvent(id) } }",
    """    fun startSoundscape(type: SoundscapeType) {
        sensorsManager.vibrateTap()
        repository.startSoundscape(type)
    }""",
    "fun stopSoundscape() { repository.stopSoundscape() }",
    "fun setSoundscapeVolume(volume: Float) { repository.setSoundscapeVolume(volume) }",
    """    fun startFocusTimerWithSoundscape(minutes: Int) {
        sensorsManager.vibrateCelebration()
        repository.startFocusTimerWithSoundscape(minutes)
    }""",
    "fun stopFocusTimerWithSoundscape() { repository.stopFocusTimerWithSoundscape() }",
    """    fun onPetTouched() {
        viewModelScope.launch {
            sensorsManager.vibrateTap()
            repository.setPetEmotion(PetEmotion.HAPPY)
        }
    }""",
    """    fun onPetPetted() {
        viewModelScope.launch {
            sensorsManager.vibratePurr()
            repository.setPetEmotion(PetEmotion.HAPPY)
        }
    }""",
    "fun setBloubShape(shape: com.example.domain.model.BloubShape) { viewModelScope.launch { repository.setBloubShape(shape) } }",
    "fun setBloubSkinColor(skinColor: com.example.domain.model.BloubSkinColor) { viewModelScope.launch { repository.setBloubSkinColor(skinColor) } }",
    "fun feedPet() { viewModelScope.launch { repository.setPetEmotion(PetEmotion.ENERGETIC) } }",
    "fun dancePet() { viewModelScope.launch { repository.setPetEmotion(PetEmotion.PLAYFUL) } }",
    "fun pokePet() { viewModelScope.launch { repository.setPetEmotion(PetEmotion.CONCERNED) } }",
    """    fun togglePetSleep() {
        viewModelScope.launch {
            if (petStatus.value.currentEmotion == PetEmotion.SLEEPY) {
                repository.setPetEmotion(PetEmotion.HAPPY)
            } else {
                repository.setPetEmotion(PetEmotion.SLEEPY)
            }
        }
    }"""
]

for func in funcs:
    content = content.replace(func, "")

# Some extra whitespace might be left, but it's fine.
with open("app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt", "w") as f:
    f.write(content)

