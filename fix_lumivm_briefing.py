with open("app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt", "r") as f:
    content = f.read()

funcs = [
    """    fun refreshDailyBriefing(type: BriefingType? = null) {
        viewModelScope.launch {
            _isBriefingGenerating.value = true
            val briefing = briefingEngine.generateBriefing(
                type = type ?: BriefingType.MORNING,
                petStatus = petStatus.value,
                petEvolution = petEvolution.value,
                tasks = allTasks.value,
                events = allCalendarEvents.value,
                wellnessLogs = allWellnessLogs.value
            )
            _dailyBriefing.value = briefing
            _isBriefingGenerating.value = false
        }
    }""",
    "fun speakBriefing() {}"
]

for func in funcs:
    content = content.replace(func, "")

# It might have slightly different whitespace. Let's just use regex.
import re
content = re.sub(r'fun refreshDailyBriefing.*?}\s*}', '', content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt", "w") as f:
    f.write(content)
