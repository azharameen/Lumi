with open("app/src/main/java/com/example/ui/components/ProactiveDailyBriefingCard.kt", "r") as f:
    content = f.read()

content = content.replace("viewModel: LumiViewModel,", "dailyBriefing: com.example.domain.briefing.DailyBriefing?,\n    onSpeakBriefing: () -> Unit,")
content = content.replace("val briefing by viewModel.dailyBriefing.collectAsState()", "")
content = content.replace("briefing", "dailyBriefing")
content = content.replace("viewModel.speakBriefing()", "onSpeakBriefing()")
content = content.replace("import com.example.ui.viewmodel.LumiViewModel", "")

with open("app/src/main/java/com/example/ui/components/ProactiveDailyBriefingCard.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/screens/lifehub/ScheduleSection.kt", "r") as f:
    content = f.read()

# Add to ScheduleSection signature
content = content.replace("onAddEvent: (com.example.data.local.entity.CalendarEventEntity) -> Unit,", "onAddEvent: (com.example.data.local.entity.CalendarEventEntity) -> Unit,\n    dailyBriefing: com.example.domain.briefing.DailyBriefing?,\n    onSpeakBriefing: () -> Unit,")

content = content.replace("viewModel = viewModel,", "dailyBriefing = dailyBriefing,\n                    onSpeakBriefing = onSpeakBriefing,")

with open("app/src/main/java/com/example/ui/screens/lifehub/ScheduleSection.kt", "w") as f:
    f.write(content)

# Add to LifeHubScreen signature
with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "r") as f:
    content = f.read()

content = content.replace("onAddEvent: (com.example.data.local.entity.CalendarEventEntity) -> Unit,", "onAddEvent: (com.example.data.local.entity.CalendarEventEntity) -> Unit,\n    dailyBriefing: com.example.domain.briefing.DailyBriefing?,\n    onSpeakBriefing: () -> Unit,")

content = content.replace("onAddEvent = onAddEvent,", "onAddEvent = onAddEvent,\n                        dailyBriefing = dailyBriefing,\n                        onSpeakBriefing = onSpeakBriefing,")

with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "w") as f:
    f.write(content)

# Update MainActivity.kt
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("onAddEvent = { event -> viewModel.addCalendarEvent(event) },", "onAddEvent = { event -> viewModel.addCalendarEvent(event) },\n                        dailyBriefing = viewModel.dailyBriefing.collectAsState().value,\n                        onSpeakBriefing = { viewModel.speakBriefing() },")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

