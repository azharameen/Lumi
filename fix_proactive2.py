with open("app/src/main/java/com/example/ui/components/ProactiveDailyBriefingCard.kt", "r") as f:
    content = f.read()

content = content.replace("val dailyBriefing by viewModel.dailyBriefing.collectAsState()\n", "")
content = content.replace("dailyBriefing: com.example.domain.briefing.DailyBriefing?,", "briefing: com.example.domain.briefing.DailyBriefing?,")
content = content.replace("val dailyBriefing = dailyBriefing ?: return", "val dailyBriefing = briefing ?: return")

with open("app/src/main/java/com/example/ui/components/ProactiveDailyBriefingCard.kt", "w") as f:
    f.write(content)
    
# also update the callers: ScheduleSection.kt and LifeHubScreen.kt to pass `briefing` instead of `dailyBriefing`? No, wait, they passed `dailyBriefing = dailyBriefing` which is fine.
# But wait, does `com.example.domain.briefing.DailyBriefing` exist?
